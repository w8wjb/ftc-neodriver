package org.firstinspires.ftc.teamcode;

    public class Timer {
        private double startTime;

        public Timer() {
            this.startTime = System.currentTimeMillis();
        }

        public boolean hasElapsed(double seconds) {
            return (System.currentTimeMillis() - startTime >= (seconds * 1000));
        }


        public void reset() {
            this.startTime = System.currentTimeMillis();
        }
    }
