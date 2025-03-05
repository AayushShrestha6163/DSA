// Class responsible for printing numbers
class NumberPrinter {
    public void printZero() {
        System.out.print("0");  // Prints '0'
    }

    public void printEven(int num) {
        System.out.print(num);  // Prints even numbers
    }

    public void printOdd(int num) {
        System.out.print(num);  // Prints odd numbers
    }
}

// Class to control synchronized printing of numbers using threads
class ThreadController {
    private final NumberPrinter printer;
    private final int n;  // Upper limit of numbers to print
    private int currentNumber = 1;  // Tracks the current number to print
    private boolean zeroTurn = true; // Boolean to track whether it's zero's turn
    private final Object lock = new Object(); // Lock object for synchronization

    // Constructor to initialize printer and limit
    public ThreadController(NumberPrinter printer, int n) {
        this.printer = printer;
        this.n = n;
    }

    // Method to start three threads: zero-printing, odd-printing, and even-printing
    public void startThreads() throws InterruptedException {
        // Thread to print '0' alternately before each number
        Thread zeroThread = new Thread(() -> {
            try {
                while (true) {
                    synchronized (lock) {
                        while (!zeroTurn) { // Wait if it's not zero's turn
                            lock.wait();
                            if (currentNumber > n) break; // Stop if number exceeds limit
                        }
                        if (currentNumber > n) break;
                        printer.printZero();
                        zeroTurn = false; // Switch turn to number printing
                        lock.notifyAll(); // Notify waiting threads
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Thread to print odd numbers
        Thread oddThread = new Thread(() -> {
            try {
                while (true) {
                    synchronized (lock) {
                        // Wait until it's not zero's turn and number is odd
                        while (zeroTurn || currentNumber % 2 == 0 || currentNumber > n) {
                            lock.wait();
                            if (currentNumber > n) break;
                        }
                        if (currentNumber > n) break;
                        printer.printOdd(currentNumber);
                        currentNumber++; // Increment number after printing
                        zeroTurn = true; // Switch turn back to zero
                        lock.notifyAll(); // Notify other threads
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Thread to print even numbers
        Thread evenThread = new Thread(() -> {
            try {
                while (true) {
                    synchronized (lock) {
                        // Wait until it's not zero's turn and number is even
                        while (zeroTurn || currentNumber % 2 != 0 || currentNumber > n) {
                            lock.wait();
                            if (currentNumber > n) break;
                        }
                        if (currentNumber > n) break;
                        printer.printEven(currentNumber);
                        currentNumber++; // Increment number after printing
                        zeroTurn = true; // Switch turn back to zero
                        lock.notifyAll(); // Notify other threads
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Start all threads
        zeroThread.start();
        oddThread.start();
        evenThread.start();

        // Ensure all threads complete execution before main thread exits
        zeroThread.join();
        oddThread.join();
        evenThread.join();
    }
}

// Main class to execute the program
public class ThreadNumber {
    public static void main(String[] args) throws InterruptedException {
        NumberPrinter printer = new NumberPrinter();
        ThreadController controller = new ThreadController(printer, 10);
        controller.startThreads(); // Start thread execution
    }
}

