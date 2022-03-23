public class ServerLogsThread extends Thread{

        private static final int START = 1;
        private static final int END = 0;
        private final String content;
        private final int type;
        private String file;


        public ServerLogsThread ( String content , String file , int start_end ) {
            this.content = content;
            this.file = file;
            this.type = start_end;
        }

        public void addContent () {
            switch ( type ) {
                case START -> file = content + file;
                case END -> file = file + content;
            }
        }


        public String getFile () {
            return this.file;
        }
}
