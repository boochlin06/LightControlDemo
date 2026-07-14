# LightControl (Emotion Light)

LightControl 是一個基於 Android 平台的智慧 IoT 燈控展示應用（Demo App），結合邊緣運算的人臉情緒辨識，並將使用者的即時情緒狀態轉化為實體的智慧燈光顏色。

---

## 程式功能分析 (Program Feature Analysis)

專案主要涵蓋三個功能核心：

1. **即時臉部情緒擷取與推論**
   * 透過 OpenCV (`JavaCameraView`) 擷取相機的即時影像串流。
   * 將影像傳遞給 Emotibot 的核心視覺引擎 (`IntelliEyeCoreManager`) 進行人臉追蹤與情緒辨識。
   * 支援 9 種情緒輸出：憤怒、厭惡、開心、悲傷、驚訝、恐懼、平靜、蔑視、困惑。

2. **智慧燈光控制邏輯**
   * 每隔一段時間（預設 10 秒，可透過 UI 動態調整）將計算出的「最高分情緒」轉換為對應的燈光顏色。
   * 透過 RESTful API (OkHttp / Retrofit) 非同步發送 JSON 格式指令（如 `{ "text": "全部的灯调成红色" }`）至 IoT 伺服器，進而控制實體燈泡。
   * 若無偵測到人臉或情緒以「平靜」為主，則發送關閉指令。

3. **情緒數值平滑化與 UI 呈現**
   * **平滑處理**：為了防止因為單一影格 (Frame) 誤判導致燈光頻繁閃爍，系統將連續蒐集的情緒分數存入滑動視窗 (Sliding Window) 計算平均值。
   * **豐富的儀表板**：UI 動態排序並顯示 Top 5 情緒的分數與直條圖，提供使用者視覺化的反饋；同時也保留了手動點擊顏色區塊直接控制燈光的功能。

---

## 檔案與目錄結構 (Directory Structure)

```text
LightControl/
├── src/main/
│   ├── AndroidManifest.xml          # 應用程式配置與權限聲明（相機、網路）
│   ├── java/com/emotibot/robotvision/demo/
│   │   ├── LightControlActivity.java        # 核心 Controller，負責 UI 綁定、相機生命週期、情緒平均演算法
│   │   ├── RemoteLightControlService.java   # 負責將情緒控制指令封裝並透過網路 (OkHttp) 傳送至 IoT 伺服器
│   │   └── LightControlService.java         # Retrofit API 介面定義
│   └── res/                         # Android 資源檔 (Layout XML, Drawable 圖片, 字串資源)
├── libs/                            # 依賴的 AAR 套件 (OpenCV, IntelliCore, dlib)
├── build.gradle                     # App 模組的建置設定與依賴管理
└── proguard-rules.pro               # ProGuard 混淆規則
```

---

## 使用到的 Design Pattern

在本專案的實作中，運用了以下幾種常見的設計模式與架構模式：

1. **Singleton Pattern (單例模式)**
   * `RemoteLightControlService` 實作了具有 Double-Checked Locking 的 Thread-safe 單例模式，確保整個 App 生命週期中只維護一個網路請求實例。
   * SDK 內部的 `IntelliEyeCoreManager` 也採用單例模式管理生命週期。
2. **Observer / Listener Pattern (觀察者 / 監聽者模式)**
   * **相機資料流**：實作 `CameraBridgeViewBase.CvCameraViewListener2` 來非同步接收 OpenCV 相機的每一幀 (Frame) 影像。
   * **網路回呼**：使用 OkHttp / Retrofit 的 `Callback` 介面，當 HTTP Request 完成或失敗時，非同步觸發 UI 執行緒的 Toast 通知。
3. **MVC / MVP 架構 (Architecture Pattern)**
   * 採用傳統的 Android 開發架構，以 `LightControlActivity` 作為主控台 (Controller)，負責協調 Model (`InferResult` / `EmotionData`) 與 View (XML Layouts)。

---

## 專案亮點介紹 (Highlights)

* ✨ **邊緣運算與 IoT 的完美結合**
  * 在終端設備（手機/平板）上直接運行高複雜度的視覺推論，避免了將影像即時上傳雲端造成的延遲與隱私問題。推論出結果後再發送極小量的控制指令給 IoT 伺服器，達到極高的反應速度。
* 🛡 **情緒平滑演算法 (Sliding Window Average)**
  * 自行封裝 `EmotionAverager` 內部類別。在連續的影像幀中，保留固定大小的歷史分數進行平均計算。這種「Low-pass filter（低通濾波）」的設計大幅降低了雜訊干擾，使得燈光變化如人類情緒一樣平緩流暢。
* 🎨 **動態資料驅動的 UI 渲染**
  * 實作了可比較的 `EmotionData` 模型，每一次計算後會重新針對 9 種情緒的分數進行排序 (`Collections.sort`)，動態更新 UI 上的前五大情緒指標，使得畫面互動性十足。
* ⚡️ **View Binding 的優雅實作**
  * 導入 `ButterKnife` 取代大量冗長的 `findViewById` 與 `setOnClickListener`，使得超過 800 行的 `LightControlActivity` 依然能保持視圖綁定程式碼的整潔與好維護。
