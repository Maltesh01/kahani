FROM node:22-alpine AS build

WORKDIR /app/web
COPY web/package.json web/package-lock.json ./
RUN npm ci
COPY web/index.html ./
COPY web/public ./public
COPY web/src ./src
COPY web/tsconfig.json web/tsconfig.app.json web/tsconfig.node.json web/vite.config.ts ./
RUN npm run build

FROM nginxinc/nginx-unprivileged:stable-alpine
COPY web/nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/web/dist /usr/share/nginx/html
EXPOSE 8080
