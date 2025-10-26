import subprocess
import time

IP = "127.0.0.1"
Port = "4000"

Client = ["java", "Client", IP, Port]

Iter = 10
DELAY = 0.125

# https://www.geeksforgeeks.org/python/python-subprocess-module/
def test(thread_count):

    process = subprocess.Popen(
        Client,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )

    process.stdin.write(f"{thread_count}\n")
    process.stdin.flush()

    for command in range(1,7):
        for _ in range(Iter):
            time.sleep(DELAY)
            process.stdin.write(f"{command}\n")
            process.stdin.flush()

    time.sleep(DELAY)
    process.stdin.write("7\n")
    process.stdin.flush()

    process.stdin.close()
    stdout_data, stderr_data = process.communicate()

def main():
    print("Starting")

    for i in range(1,26):
        print("Running for thread count ", i)
        test(i)
        time.sleep(DELAY)

    print("Done")

if __name__ == "__main__":
    main()