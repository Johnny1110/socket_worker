# socket-worker 系列

<br>

---

<br>
<br>

## 簡介

<br>

這一個套件在做的就是啟動一個 socket，然後接收資料，資料的處裡可以通過實作 RecordReader 介面來實現不同的商業邏輯。socket 主要分為 4 種：

* IO Socket

* NIO Socket

* SSL IO Socket

* SSL NIO Socket

<br>

不僅可以使用一般 IO 或者 NIO，還可以加入 SSL。

<br>

下面會依照 package 的分類分別做介紹。

<br>
<br>

## Package

<br>

* [core](doc/core)

* [exception](doc/exception)

* [impl](doc/impl)

* [reader](doc/reader)

* [utils](doc/utils)

* [使用](doc/use)

* [jsk 憑證製作](doc/jsk)
