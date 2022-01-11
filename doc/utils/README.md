# Utils

<br>

---

<br>

## GZIPStringUtils

<br>

這是一個壓縮套件，裡面有 2 個方法：

* `public static byte[] compress(byte[] bytesData)`

* `public static byte[] uncompress(byte[] compressedContent)`

<br>

傳入 `byte` 陣列進行壓縮與解壓縮動作。進行這個壓縮動作可以在傳輸大量資料的時候減少封包大小，將低頻寬負荷，但相對的在壓縮與解壓縮的過程也同時增加了運算負擔。

<br>

