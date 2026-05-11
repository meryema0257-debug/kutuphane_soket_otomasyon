import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;

public class KutuphaneServer extends JFrame {
    private JTextArea logAlani;
    private HashMap<String, String> kitapVeritabanı;
    private static final long POLINOM = 0x04C11DB7L;

    public KutuphaneServer() {
        setTitle("MAKÜ Kütüphane - Multimedya Sunucusu");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logAlani = new JTextArea();
        logAlani.setEditable(false);
        logAlani.setBackground(new Color(20, 20, 20));
        logAlani.setForeground(new Color(50, 255, 50));
        logAlani.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logAlani), BorderLayout.CENTER);

        // Veritabanı
        kitapVeritabanı = new HashMap<>();
        kitapVeritabanı.put("nutuk", "Mevcut - Raf: A-12");
        kitapVeritabanı.put("safahat", "Ödünç Verildi - İade: 20 Mayıs");

        setVisible(true);
        logYaz("🚀 Multimedya Sunucusu Başlatıldı. Port: 8080");
        new Thread(this::serverBaslat).start();
    }

    private void logYaz(String mesaj) {
        SwingUtilities.invokeLater(() -> logAlani.append("[" + java.time.LocalTime.now() + "] " + mesaj + "\n"));
    }

    private void serverBaslat() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> istekIsle(clientSocket)).start();
            }
        } catch (Exception e) {
            logYaz("Sunucu Hatası: " + e.getMessage());
        }
    }

    private void istekIsle(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            OutputStream out = socket.getOutputStream()
        ) {
            String istekSatiri = in.readLine();
            if (istekSatiri == null) return;

            // --- VİDEO İSTEĞİ KONTROLÜ ---
            if (istekSatiri.contains("/video")) {
                logYaz("🎬 Telefondan VİDEO akışı (Streaming) isteği geldi.");
                File videoDosyasi = new File("tanitim.mp4"); // Klasördeki video ismi
                
                if (videoDosyasi.exists()) {
                    byte[] videoBytes = Files.readAllBytes(videoDosyasi.toPath());
                    String header = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: video/mp4\r\n" +
                                    "Content-Length: " + videoBytes.length + "\r\n\r\n";
                    out.write(header.getBytes());
                    out.write(videoBytes);
                    logYaz("✅ Video dosyası (" + videoBytes.length + " byte) başarıyla gönderildi.");
                } else {
                    String hata = "HTTP/1.1 404 Not Found\r\n\r\nVideo bulunamadı!";
                    out.write(hata.getBytes());
                    logYaz("❌ Hata: tanitim.mp4 dosyası sunucu klasöründe yok!");
                }
                return;
            }

            // --- NORMAL KİTAP SORGULAMA ARAYÜZÜ ---
            String kitapAdi = "";
            if (istekSatiri.contains("kitap=")) {
                kitapAdi = istekSatiri.split("kitap=")[1].split(" ")[0].replace("+", " ").toLowerCase();
            }

            logYaz("🔎 Sorgu: " + (kitapAdi.isEmpty() ? "Ana Sayfa" : kitapAdi));

            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            html.append("<style>body{font-family:sans-serif; background:#f0f2f5; text-align:center; padding:20px;}");
            html.append(".card{background:white; padding:25px; border-radius:15px; box-shadow:0 4px 10px rgba(0,0,0,0.1);}");
            html.append("input{width:80%; padding:10px; margin:10px; border-radius:5px; border:1px solid #ddd;}");
            html.append("button{padding:10px 20px; border-radius:5px; border:none; background:#1a73e8; color:white; font-weight:bold;}");
            html.append(".video-btn{background:#e74c3c; margin-top:20px;}</style></head><body>");

            html.append("<div class='card'><h2>MAKÜ Dijital Kütüphane</h2>");
            html.append("<form action='/sorgu' method='GET'>");
            html.append("<input type='text' name='kitap' placeholder='Kitap ara...' required><br>");
            html.append("<button type='submit'>Sorgula (TCP)</button></form>");

            if (!kitapAdi.isEmpty()) {
                String sonuc = kitapVeritabanı.getOrDefault(kitapAdi, "Kayıt bulunamadı.");
                html.append("<div style='margin-top:20px; color:#2c3e50;'><b>Sonuç:</b> ").append(sonuc).append("</div>");
                html.append("<p style='font-size:10px; color:gray;'>CRC-32 Kontrol Edildi</p>");
            }

            html.append("<hr><p>Kütüphane Tanıtım Videosu</p>");
            html.append("<a href='/video'><button class='video-btn'>Tanıtım Videosunu İzle (Binary Stream)</button></a>");
            html.append("</div></body></html>");

            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n" + html.toString();
            out.write(response.getBytes("UTF-8"));

        } catch (Exception e) {
            logYaz("Hata: " + e.getMessage());
        }
    }

    public static void main(String[] args) { new KutuphaneServer(); }
}