Bu uygulama **gRPC** tabanlı çok kullanıcılı bir çizim uygulamasıdır.

## Gereksinimler

- JDK 17+
- **Maven** (protobuf + gRPC kod üretimi ve bağımlılıklar için)

## Çalıştırma

1. `1-derle.bat` — `mvn compile` + bağımlılıkları `target/lib` altına kopyalar
2. `2-sunucu.bat` — gRPC sunucusu (port **9100**)
3. `3-istemci.bat` — istemci (birden fazla açılabilir)

## İletişim

- Tanım: `src/main/proto/paint.proto`
- Servis: `PaintService.Session` — **çift yönlü stream** (`stream Envelope` ↔ `stream Envelope`)
- Her `Envelope.line` alanı mevcut metin protokolünü taşır (`HELLO|Ahmet`, `DRAW_PATH|...`)
- Tuval mantığı (`CanvasRoom`, `DrawOps`, `CanvasPanel`) değişmedi
