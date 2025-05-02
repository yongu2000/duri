# 1단계: 빌드
FROM node:22-alpine AS builder
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build

# 2단계: 런타임
FROM node:22-alpine
WORKDIR /app
COPY --from=builder /app .
RUN npm install --omit=dev
EXPOSE 3000
CMD ["npm", "run", "start"]