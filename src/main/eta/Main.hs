import Control.Monad
import Control.Concurrent.MVar hiding (takeMVar, putMVar)
import qualified Control.Concurrent.MVar as MVar
import Control.Concurrent.Fiber
import Control.Concurrent.Fiber.MVar
import System.Environment
import GHC.Conc.Sync hiding (yield)
import GHC.Conc.IO
import Java

ring = 503

new l i = do
  r   <- newEmptyMVar
  ret <- newEmptyMVar
  forkFiber (thread ret i l r)
  return (r, ret)

thread :: MVar Int -> Int -> MVar Int -> MVar Int -> Fiber ()
thread ret i l r = go
  where go = do
          m <- takeMVar l
          putMVar r $! m - 1
          if (m < 1)
          then putMVar ret m
          else go

threadring :: Int -> Int -> IO JIntArray
threadring ring msgs = do
  setNumCapabilities 1
  a       <- newMVar msgs
  ret     <- newEmptyMVar
  (z, xs) <- foldM (\(prev, xs) i -> do
                   (r, ret) <- new prev i
                   return (r, ret:xs))
               (a, []) [2..ring]
  forkFiber (thread ret 1 z a)
  ints <- mapM MVar.takeMVar (reverse (ret : xs))
  return $ toJava ints

foreign export java "@static eta.threadring.ThreadRing.start"
  threadring :: Int -> Int -> IO JIntArray

main :: IO ()
main = do
  msgs <- fmap (read . head) getArgs
  threadring ring msgs
  return ()
