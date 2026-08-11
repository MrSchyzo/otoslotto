package com.mrschyzo.hungarian.domain;

import com.mrschyzo.hungarian.domain.pick.Pick;

public interface Lottery {
    void acceptPick(Pick pick);

    Histogram getWinnersHistogram(Pick winningOne);

    class Histogram {
        private int[] hits;
        protected Histogram(int[] hits) {
            this.hits = hits;
        }

        public int get2MatchCount() {
            return hits[2];
        }

        public int get3MatchCount() {
            return hits[3];
        }

        public int get4MatchCount() {
            return hits[4];
        }

        public int get5MatchCount() {
            return hits[5];
        }
    }
}
