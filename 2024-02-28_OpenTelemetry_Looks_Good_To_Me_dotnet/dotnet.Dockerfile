FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
COPY ./src /src
WORKDIR /src
RUN dotnet build -c Release -o /app/build

FROM build AS publish
RUN dotnet publish \
            --configuration Release \ 
            --runtime linux-x64 \
            --self-contained \ 
            --output /app/publish \
            -p:PublishReadyToRun=true  

FROM mcr.microsoft.com/dotnet/aspnet:8.0
USER 1000
WORKDIR /workspace
COPY --chown=1000:1000 --from=publish /app/publish /workspace 
ENTRYPOINT [ "/workspace/app" ]