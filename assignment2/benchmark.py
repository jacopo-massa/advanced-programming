import csv
import functools
import os
import time
from threading import Thread

import requests
from requests.exceptions import ConnectionError, Timeout, HTTPError

""" --- Exercise 6 - A decorator for benchmarking --- """


# To test this exercise and the next one, we use a function that computes the n-th Fibonacci number
# in the standard, inefficient, double recursive way
def fibonacci(n):
    if n <= 2:
        return 1
    else:
        return fibonacci(n - 1) + fibonacci(n - 2)


# auxiliary function for timing the execution of a passed function
# with the passed arguments
def timing(fun, *args, **kwargs):
    start_time = time.perf_counter()
    fun(*args, **kwargs)
    end_time = time.perf_counter()
    run_time = end_time - start_time
    return run_time


# definition of the decorator
def benchmark(warmups=0, iter=1, verbose=False, csv_file=None):
    def decorator(fun):
        @functools.wraps(fun)
        def wrapper_benchmark(*args, **kwargs):
            # list to store the execution times
            time_results = []
            # init the writer on the csv file, if it is specified
            if csv_file:
                file = open(csv_file, 'w', newline='')
                writer = csv.writer(file)
                writer.writerow(["run num", "is warmup", "timing"])
            # else branch is to avoid warnings.
            # Never used with value None, because it'always called when csv_file is not None
            else:
                writer = None
                file = None

            print("run num\t | warmup | timing\t ")
            # call the function a number of times equal to 'warmups'
            for i in range(warmups):
                run_time = timing(fun, *args, **kwargs)
                if verbose:
                    # print(f"Finished {i + 1}^ warmup of {fun.__name__!r} in {run_time:.4f} secs")
                    print(f"{i + 1}^\t | {True}\t  | {run_time:.4f} secs")
                if csv_file:
                    writer.writerow([i + 1, "True", " --- "])

            # call the function a number of times equal to 'iter'
            for i in range(iter):
                run_time = timing(fun, *args, **kwargs)
                # result are stored only here because we're doing normal invokations and not warm-up ones.
                time_results.append(run_time)
                if verbose:
                    # print(f"Finished {i + 1}^ iter of {fun.__name__!r} in {run_time:.4f} secs")
                    print(f"{i + 1}^\t | {False}  | {run_time:.4f} secs")
                if csv_file:
                    writer.writerow([i + 1, "False", "{0:.4f}".format(run_time)])

            if file:
                file.close()

            # print benchmark statistics
            # --- average
            time_avg = sum(time_results) / len(time_results)
            print(f"AVG TIME: {time_avg:.4f}")
            # --- variance
            time_var = sum((x - time_avg) ** 2 for x in time_results) / len(time_results)
            print(f"VARIANCE: {time_var:.8f}")

        return wrapper_benchmark

    return decorator


""" Exercise 7 - Testing the decorator with multithreading """


# create a class to launch the thread, overriding the 'run' method
# in order to run the passed function several times
class MyThread(Thread):
    def __init__(self, function, times, *args, **kwargs):
        Thread.__init__(self)
        self.function = function
        self.times = times
        self.args = args
        self.kwargs = kwargs

    def run(self):
        for i in range(self.times):
            # uncomment next line to see how threads are interleaved
            # print("thread {}, {}° time".format(get_ident(), i + 1))
            self.function(*self.args, **self.kwargs)


def test(f, *args, **kwargs):
    warmups = 5
    iterations = 3

    n_th = 1
    n_it = 16

    def thread_pool(num_threads, num_iterations):
        threads = []
        for i in range(num_threads):
            threads.append(MyThread(f, num_iterations, *args, **kwargs))
            threads[i].start()

        for t in threads:
            t.join()

        threads.clear()

    for _ in range(4):
        print("--- {} THREAD / {} ITER ---".format(n_th, n_it))

        @benchmark(warmups, iterations, True, "f_{}_{}.csv".format(n_th, n_it))
        def threadify():
            thread_pool(n_th, n_it)

        threadify()
        n_th *= 2
        n_it //= 2


"""
BRIEF COMMENT ABOUT RESULTS
The execution time is not affected by changing the degree of parallelism (so the number of threads)
because of the GIL (Global Interpreter Lock), which is the mutex that protects access to Python objects, 
and most importantly avoids multiple threads can execute Python code at once.
"""

""" Exercise 8 - [Optional] Downloading and executing Python scripts """

PRE_SCRIPT_PATH = "/tmp/pre_script.py"
POST_SCRIPT_PATH = "/tmp/post_script.py"

PRE_URL = "http://pages.di.unipi.it/corradini/Didattica/AP-19/PROG-ASS/02/pre.py"
POST_URL = "http://pages.di.unipi.it/corradini/Didattica/AP-19/PROG-ASS/02/post.py"


def prepost(pre_url=None, post_url=None):
    if not pre_url or not post_url:
        raise ValueError("Both urls are mandatory!")

    # try to download the scripts and save them into temporary files
    try:
        pre_script = requests.get(pre_url)
        pre_script.raise_for_status()
        with open(PRE_SCRIPT_PATH, 'wb') as f:
            f.write(pre_script.content)

        post_script = requests.get(post_url)
        post_script.raise_for_status()
        with open(POST_SCRIPT_PATH, 'wb') as f:
            f.write(post_script.content)

    # manage case of connection error or bad response from the server
    except (ConnectionError, Timeout):
        print("Server is down")
        exit(-1)
    except HTTPError:
        print("Server is not responding correctly")
        exit(-1)
    else:
        def decorator(fun):
            @functools.wraps(fun)
            def wrapper_prepost(*args, **kwargs):
                print("1) Executing pre-script...")
                os.system("python3 {}".format(PRE_SCRIPT_PATH))
                os.remove(PRE_SCRIPT_PATH)

                print(f"\n2) Executing function {fun.__name__!r}...")
                fun_result = fun(*args, **kwargs)

                print("\n3) Executing post-script...")
                os.system("python3 {}".format(POST_SCRIPT_PATH))
                os.remove(POST_SCRIPT_PATH)
                return fun_result
            return wrapper_prepost
        return decorator


@prepost(pre_url=PRE_URL, post_url=POST_URL)
def test2():
    print(fibonacci(20))


if __name__ == '__main__':
    # exercise 6-7
    test(fibonacci, 28)
    print("\n")
    # exercise 8
    test2()
