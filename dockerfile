# ─────────────────────────────────────────────
# Imagem base: Java 21 (JDK slim, sem Maven)
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine
 
# Diretório de trabalho dentro do container
WORKDIR /app
 
# Copia todo o projeto para dentro do container
COPY . .
 
# Compila todos os .java de uma vez durante o build da imagem.
# Coloca os .class em /app/bin
RUN find src -name "*.java" | xargs javac -d bin
 
# Volume onde os XMLs gerados serão salvos.
# Mapeia para o diretório output/ da sua máquina via docker-compose.
VOLUME ["/app/output"]
 
# Comando padrão: roda o runner de validação completa.
# Pode ser sobrescrito na hora do `docker run` ou no compose.
CMD ["java", "-cp", "bin", "br.com.jackcompiler.FilesAndValidationRunner"]