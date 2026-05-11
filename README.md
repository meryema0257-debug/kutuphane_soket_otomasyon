# kutuphane_soket_otomasyon
# Java & Android Soket Programlama Projesi

Bu proje, bir kütüphane otomasyon sisteminin sunucu-istemci (Server-Client) mimarisini simüle eder.

## Özellikler
* **TCP Protokolü:** Kitap sorgulama ve veri transferi için güvenilir TCP bağlantısı kullanılmıştır.
* **HTTP Arayüzü:** Android emülatörler (Panda2 vb.) ile uyumlu web tabanlı istemci arayüzü.
* **CRC-32 Hata Kontrolü:** Veri bağı katmanında bütünlük doğrulaması simülasyonu.
* **Multimedya Transferi:** Sunucu üzerinden video akışı (streaming) desteği.
* **Android Uyumu:** 10.0.2.2 Gateway adresi üzerinden emülatör haberleşmesi.

## Nasıl Çalıştırılır?
1. `KutuphaneServer.java` dosyasını derleyin ve çalıştırın.
2. Android emülatörden `http://10.0.2.2:8080` adresine gidin.
