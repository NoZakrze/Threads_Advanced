package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
    private static class CachingPrimeChecker {
        private final Map<Long,Boolean> cache = new HashMap<>();
        private final ReadWriteLock lock = new ReentrantReadWriteLock();


        public boolean isPrime(final long x){
            lock.readLock().lock();
            try
            {
                   if(cache.containsKey(x))
                   {
                       System.out.printf("Cached result for %d: %b%n", x, cache.get(x));
                       return cache.get(x);
                   }
            }
            finally
            {
                lock.readLock().unlock();
            }
            boolean result = computeIfPrime(x);
            lock.writeLock().lock();
            try
            {
                cache.put(x,result);
            }
            finally
            {
                lock.writeLock().unlock();
            }
            return result;
        }
        private boolean computeIfPrime(long x) {
            final String currentThreadName = Thread.currentThread().getName();
            System.out.printf("\t[%s] Running computation for: %d%n", currentThreadName, x);
            try {
                TimeUnit.SECONDS.sleep(10); // Simulating long computations
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (x < 2) {
                return false;
            }
            for (long i = 2; i * i <= x; i++) {
                if (x % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }
    public static void main(String[] args) throws InterruptedException {
        final CachingPrimeChecker primeChecker = new CachingPrimeChecker();
        final ExecutorService executor = Executors.newFixedThreadPool(4);
        final Scanner scanner = new Scanner(System.in);
        int koniec = 0;
        while (koniec == 0) {
            Long[] numbers = new Long[4];
            System.out.println("Enter 4 numbers:");
            for (int i = 0; i < 4; i++) {
                numbers[i] = scanner.nextLong();
            }

            Future<Boolean>[] futures = new Future[4];
            for (int i = 0; i < 4; i++) {
                final long num = numbers[i];
                if (num == -1) {
                    koniec = 1;
                    break;
                }
                futures[i] = executor.submit(() -> primeChecker.isPrime(num));
            }

            if (koniec == 1) break;

            try {
                for (int i = 0; i < 4; i++) {
                    if (futures[i] != null) {
                        boolean result = futures[i].get();
                        System.out.println("Is prime: " + result);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        executor.awaitTermination( 60 , TimeUnit.SECONDS );


    }
}
