cd /server
mkdir "errorPages"
mkdir "imageCache"
ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
nohup java -jar -Duser.timezone=GMT+08 /server/weihashi.jar > /server/log/whsLog$PORT.txt 2>&1