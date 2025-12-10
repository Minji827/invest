import time
import requests
from datetime import datetime

# Render 서버 주소
URL = "https://invest-qviy.onrender.com/api/stock/info?symbol=AAPL" # 가벼운 API 호출
INTERVAL = 600 # 10분 (초 단위)

def keep_alive():
    print(f"🔄 Keep-Alive 스크립트 시작: {URL}")
    print(f"⏰ {INTERVAL}초마다 서버에 요청을 보냅니다.")
    
    while True:
        try:
            current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            response = requests.get(URL)
            
            if response.status_code == 200:
                print(f"[{current_time}] ✅ 서버 생존 확인 (Status: {response.status_code})")
            else:
                print(f"[{current_time}] ⚠️ 응답 이상 (Status: {response.status_code})")
                
        except Exception as e:
            print(f"[{current_time}] ❌ 요청 실패: {e}")
            
        time.sleep(INTERVAL)

if __name__ == "__main__":
    keep_alive()
