function start() {
	cd /Applications/SmartFoxServer_2X/SFS2X/
	./sfs2x-service start
	mysql.server start
}

function restart() {
	cd /Applications/SmartFoxServer_2X/SFS2X/
  ./sfs2x-service stop
	./sfs2x-service start
}

echo "============================== Slot Machine Server =============================="

case $1 in
  "start" | "1")
    start
    ;;
  "restart" | "2")
    restart
    ;;
  *)
    echo "Tasks:"
    echo "1.  start mysql and sfs server"
    echo "2.  stop then start sfs server"
    ;;
esac
