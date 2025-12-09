
import sys
import os
import time

# Add backend directory to path so we can import app
sys.path.append(os.path.join(os.getcwd(), 'backend'))

try:
    from app import app
except ImportError:
    # If running from inside backend dir
    sys.path.append(os.getcwd())
    from app import app

def test_endpoints():
    print("Starting verification for YFinance migration...")
    client = app.test_client()

    TICKER = "AAPL"

    # 1. Test Info
    print(f"\n[TEST] Testing /api/stock/info?ticker={TICKER}...")
    response = client.get(f'/api/stock/info?ticker={TICKER}')
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        data = response.get_json()['data']
        print(f"Success! Name: {data['shortName']}, Price: {data['currentPrice']}")
    else:
        print(f"FAILED: {response.get_data(as_text=True)}")

    # 2. Test Financials
    print(f"\n[TEST] Testing /api/stock/financials?ticker={TICKER}...")
    response = client.get(f'/api/stock/financials?ticker={TICKER}')
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        data = response.get_json()['data']
        if data:
            print(f"Success! Revenue: {data[0]['revenue']}")
        else:
            print("Success but empty data (might be acceptable)")
    else:
        print(f"FAILED: {response.get_data(as_text=True)}")

    # 3. Test Metrics
    print(f"\n[TEST] Testing /api/stock/metrics?ticker={TICKER}...")
    response = client.get(f'/api/stock/metrics?ticker={TICKER}')
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        data = response.get_json()['data']
        print(f"Success! PER: {data['per']}, ROE: {data['roe']}")
    else:
        print(f"FAILED: {response.get_data(as_text=True)}")
        
    # 4. Test Dividend
    print(f"\n[TEST] Testing /api/stock/dividend?ticker={TICKER}...")
    response = client.get(f'/api/stock/dividend?ticker={TICKER}')
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        data = response.get_json()['data']
        print(f"Success! Yield: {data['dividendYield']}%")
    else:
        print(f"FAILED: {response.get_data(as_text=True)}")

    # 5. Test Search
    print(f"\n[TEST] Testing /api/stock/search?q=Apple...")
    response = client.get(f'/api/stock/search?q=Apple')
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        data = response.get_json()['data']
        print(f"Success! Found {len(data)} results.")
        if data:
            print(f"First result: {data[0]['symbol']} - {data[0]['shortName']}")
    else:
        print(f"FAILED: {response.get_data(as_text=True)}")

if __name__ == "__main__":
    test_endpoints()
