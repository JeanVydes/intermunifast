# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

# Accept build arguments
ARG VITE_API_URL=http://localhost:8080
ARG VITE_PRODUCTION=false

# Set environment variables for build
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_PRODUCTION=$VITE_PRODUCTION

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Stage 2: Development server (Vite)
FROM node:20-alpine

WORKDIR /app

# Accept build arguments
ARG VITE_API_URL=http://localhost:8080
ARG VITE_PRODUCTION=false

# Set environment variables
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_PRODUCTION=$VITE_PRODUCTION

# Copy package files
COPY package*.json ./

# Install dependencies (including dev dependencies for Vite)
RUN npm ci

# Copy built files and source
COPY --from=build /app .

# Expose port
EXPOSE 5173

# Run Vite dev server with host flag to allow external connections
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
