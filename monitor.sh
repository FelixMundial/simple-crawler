#/bin/bash
while true;
do
    isAlive=$(ps -ef | grep webmagic-demo-0.0.1-SNAPSHOT.jar | grep -v grep|wc -l)
    if [ "$isAlive" -eq 0 ]; then
        sh /home/webmagic/webmagic.sh
        echo $(date +%Y-%m-%d) $(date +%H:%M:%S) "Restarting Spider" >> /home/webmagic/message.log
    fi
    sleep 30
done