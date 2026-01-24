# Qrc Kit

> **Qrc Kit** 是一个高效的歌词处理工具，专为 QQ 音乐 `qrc` 格式设计。支持本地文件解密、远程歌词下载

---

## 🚀 核心功能

* **QRC 解密**：将加密的 `.qrc` 字符串或文件还原为标准 XML 格式。
* **一键下载**：通过 Song ID 获取完整歌词数据。

---

## 🛠️ 使用指南

### 1. 歌词解密 (Decryption)

将 QQ 音乐特有的加密字符串解密为可读的 XML 文本。

```kotlin
// 1. 定义加密的 QRC 数据
val encryptedData = "E9056DD20F5EB670F81ED60AB3D2C4CC..."

// 2. 调用解密器
val xmlResult = QrcDecrypter.decrypt(encryptedData)

// 3. 输出解密后的 XML 结构
println(xmlResult)
```

**输出示例：**

```html

<Lyric_1 LyricType="1" LyricContent="[10140,3720]去(10140,120)到(10260,300)每(10560,150)..."/>
```

### 2. 下载并自动解析 (Download & Parse)

直接获取结构化的歌词对象，包含了翻译与罗马音。

```kotlin
// 使用 Song ID 下载
val response: LyricResponse = QrcDownloader.downloadLyrics("269741123")
val data: LyricData = response.lyricData

data.richLyricLine.forEach {
    println(it)
}
```

**输出示例：**

```text
RichLyricLine(start=0, end=4050, duration=4050, text=Love Story - Taylor Swift, translation=QQ音乐享有本翻译作品的著作权, roma=null, words=[LyricWord(start=0, end=648, duration=648, text=Love), LyricWord(start=648, end=810, duration=162, text= ), LyricWord(start=810, end=1620, duration=810, text=Story), LyricWord(start=1620, end=1782, duration=162, text= ), LyricWord(start=1782, end=1944, duration=162, text=-), LyricWord(start=1944, end=2106, duration=162, text= ), LyricWord(start=2106, end=3078, duration=972, text=Taylor), LyricWord(start=3078, end=3240, duration=162, text= ), LyricWord(start=3240, end=4050, duration=810, text=Swift)])
RichLyricLine(start=4050, end=8100, duration=4050, text=Lyrics by：Taylor Swift, translation=//, roma=null, words=[LyricWord(start=4050, end=4628, duration=578, text=Lyrics), LyricWord(start=4628, end=5206, duration=578, text= ), LyricWord(start=5206, end=5784, duration=578, text=by), LyricWord(start=5784, end=6362, duration=578, text=：), LyricWord(start=6362, end=6940, duration=578, text=Taylor), LyricWord(start=6940, end=7518, duration=578, text= ), LyricWord(start=7518, end=8096, duration=578, text=Swift)])
RichLyricLine(start=8100, end=12150, duration=4050, text=Composed by：Taylor Swift, translation=//, roma=null, words=[LyricWord(start=8100, end=8678, duration=578, text=Composed), LyricWord(start=8678, end=9256, duration=578, text= ), LyricWord(start=9256, end=9834, duration=578, text=by), LyricWord(start=9834, end=10412, duration=578, text=：), LyricWord(start=10412, end=10990, duration=578, text=Taylor), LyricWord(start=10990, end=11568, duration=578, text= ), LyricWord(start=11568, end=12146, duration=578, text=Swift)])
RichLyricLine(start=12150, end=16200, duration=4050, text=Produced by：Nathan Chapman/Taylor Swift, translation=//, roma=null, words=[LyricWord(start=12150, end=12518, duration=368, text=Produced), LyricWord(start=12518, end=12886, duration=368, text= ), LyricWord(start=12886, end=13254, duration=368, text=by), LyricWord(start=13254, end=13622, duration=368, text=：), LyricWord(start=13622, end=13990, duration=368, text=Nathan), LyricWord(start=13990, end=14358, duration=368, text= ), LyricWord(start=14358, end=14726, duration=368, text=Chapman), LyricWord(start=14726, end=15094, duration=368, text=/), LyricWord(start=15094, end=15462, duration=368, text=Taylor), LyricWord(start=15462, end=15830, duration=368, text= ), LyricWord(start=15830, end=16198, duration=368, text=Swift)])
RichLyricLine(start=16206, end=19939, duration=3733, text=We were both young when I first saw you, translation=当我第一次看见你的时候 我们都还年轻, roma=null, words=[LyricWord(start=16206, end=16446, duration=240, text=We ), LyricWord(start=16446, end=16750, duration=304, text=were ), LyricWord(start=16750, end=17238, duration=488, text=both ), LyricWord(start=17238, end=18033, duration=795, text=young ), LyricWord(start=18033, end=18300, duration=267, text=when ), LyricWord(start=18300, end=18544, duration=244, text=I ), LyricWord(start=18544, end=19001, duration=457, text=first ), LyricWord(start=19001, end=19536, duration=535, text=saw ), LyricWord(start=19536, end=19920, duration=384, text=you)])
```

---

## 🔗 致谢

本项目基于以下开源实现进行封装与优化：

* [WXRIW/QQMusicDecoder](https://github.com/WXRIW/QQMusicDecoder) - 核心解密算法