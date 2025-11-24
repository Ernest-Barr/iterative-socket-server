import subprocess

IP = "127.0.0.1"
Port = "4000"
Client = ["java", "Client", IP, Port]

Iter = 10


# https://www.geeksforgeeks.org/python/python-subprocess-module/
def test(thread_count):
    process = subprocess.Popen(
        Client,
        stdin=subprocess.PIPE,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.PIPE,
        text=True,
        bufsize=1
    )

    process.stdin.write(f"{thread_count}\n")
    process.stdin.flush()

    for command in range(1, 7):
        for _ in range(Iter):
            process.stdin.write(f"{command}\n")
            process.stdin.flush()

    process.stdin.write("7\n")
    process.stdin.flush()
    process.communicate()


def main():
    print("Starting test")

    tc = [1, 5, 10, 15, 20, 25 ,100]
    for i in tc:
        print("Running for thread count ", i)
        test(i)

    print("Finished test, all results appended to data/res.csv")


if __name__ == "__main__":
    main()
