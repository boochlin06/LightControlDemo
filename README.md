# LightControl (Emotion Light)

LightControl 是一個基於 Android 平台的智慧燈控展示應用（Demo App），它能夠透過裝置鏡頭捕捉使用者的臉部表情，進行即時的情緒分析，並根據辨識出的情緒狀態自動改變智慧燈泡的顏色，達到「情緒燈光」的互動體驗。

## 功能介紹 (Features)

*   **即時情緒辨識 (Real-time Emotion Recognition)**：
    *   使用裝置前置或後置鏡頭擷取即時影像。
    *   支援辨識 9 種不同的情緒狀態：憤怒 (Angry)、厭惡 (Disgust)、開心 (Happy)、悲傷 (Sad)、驚訝 (Surprise)、恐懼 (Fear)、平靜 (Neutral)、蔑視 (Contempt)、困惑 (Confused)。
*   **智慧燈光連動 (Smart Light Control)**：
    *   根據偵測到的主要情緒，將燈光調整為對應的顏色。例如：生氣可能對應紅色，開心對應粉紅色等。
    *   當未偵測到臉部或情緒為平靜時，支援自動關閉燈光或保持預設狀態。
*   **情緒數值平滑化 (Emotion Smoothing)**：
    *   實作 `EmotionAverager`，透過滑動視窗 (Sliding Window) 計算情緒平均值，避免因單一影格的誤判造成燈光頻繁閃爍，提供更穩定的體驗。
*   **手動/細部控制 (Manual Override)**：
    *   提供手動介面，讓使用者可以點擊按鈕直接發送指定顏色的燈光控制指令。
    *   提供詳細的各項情緒信心分數顯示面板。

## 軟體開發架構 (Software Architecture)

本專案採用傳統的 Android MVC/MVP 混合架構，並高度依賴第三方視覺與網路通訊套件：

### 核心模組

1.  **UI/Presentation Layer (展示層)**：
    *   `LightControlActivity`：應用程式的主要與唯一入口，負責管理 Camera Preview、UI 元件的綁定與更新，以及使用者操作。
    *   **View Binding**：使用 [ButterKnife](http://jakewharton.github.io/butterknife/) 進行 UI 元件綁定，減少 `findViewById` 的樣板程式碼。
2.  **Vision & Emotion Engine (視覺與情緒引擎)**：
    *   **OpenCV**：整合了 `JavaCameraView` 與 `opencv_java3` 函式庫，負責高效率的即時影像串流捕捉與色彩空間轉換 (RGBA to BGR)。
    *   **IntelliEyeCore**：使用 Emotibot 提供的專屬 SDK (`IntelliEyeCoreManager`) 進行核心的臉部偵測與情緒推論，並回傳包含 9 種情緒分數的 `InferResult`。
3.  **Network & Control Layer (網路與控制層)**：
    *   `RemoteLightControlService`：負責將 UI 決定好的情緒轉換成燈控指令（例如 JSON 格式：`{ "text": "全部的灯调成红色", "customInfo": { "deviceId": "1" } }`）。
    *   **OkHttp3**：作為底層網路通訊客戶端，以非同步 (Asynchronous) 的方式發送 HTTP 請求給 IoT 伺服器或直接發送給智慧燈泡控制器。

### 開發環境與依賴

*   **Min SDK Version**: 19 (Android 4.4 KitKat)
*   **Target SDK Version**: 24 (Android 7.0 Nougat)
*   **核心套件**:
    *   `org.opencv:opencv-android` (v3.2.0)
    *   `com.emotibot:IntelliEyeCore` / `dlib-debug` (Emotion Inference)
    *   `com.squareup.okhttp3:okhttp` (Networking)
    *   `com.jakewharton:butterknife` (View Injection)

## 執行與編譯

1.  請確保有正確安裝 Android SDK 以及 NDK (若 OpenCV 有原生依賴)。
2.  專案根目錄執行 `./gradlew build` 或直接透過 Android Studio 開啟專案進行編譯與安裝。
3.  請授權 App 相機 (`CAMERA`) 與網路 (`INTERNET`) 存取權限。
