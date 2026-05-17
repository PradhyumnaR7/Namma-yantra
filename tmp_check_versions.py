import urllib.request

urls = [
    'https://dl.google.com/dl/android/maven2/com/google/firebase/firebase-auth-ktx/maven-metadata.xml',
    'https://dl.google.com/dl/android/maven2/com/google/firebase/firebase-database-ktx/maven-metadata.xml',
    'https://dl.google.com/dl/android/maven2/com/google/firebase/firebase-firestore-ktx/maven-metadata.xml',
    'https://dl.google.com/dl/android/maven2/com/google/firebase/firebase-crashlytics-ktx/maven-metadata.xml',
    'https://dl.google.com/dl/android/maven2/com/google/firebase/firebase-analytics-ktx/maven-metadata.xml',
    'https://dl.google.com/dl/android/maven2/com/google/android/gms/play-services-location/maven-metadata.xml',
]

for url in urls:
    print('URL:', url)
    try:
        with urllib.request.urlopen(url, timeout=15) as r:
            data = r.read().decode('utf-8')
            print(data[:400])
    except Exception as e:
        print('ERROR:', type(e).__name__, e)
    print('---')
