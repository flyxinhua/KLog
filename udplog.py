import socket

UDP_IP = "0.0.0.0"  # 监听所有网络接口
UDP_PORT = 9999     #  修改为你的端口号

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((UDP_IP, UDP_PORT))

print(f"Listening for UDP packets on port {UDP_PORT}...")

try:
    while True:
        data, addr = sock.recvfrom(4096) # buffer size is 1024 bytes
        print(f"{data.decode('utf-8')}")
except KeyboardInterrupt:
    print("\nListener stopped.")
finally:
    sock.close()
