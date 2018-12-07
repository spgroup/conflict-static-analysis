package br.unb.cic.analysis.samples;

import br.unb.cic.analysis.model.Pair;

import java.util.List;

public class BillingSystem {

    class Item {
        public double getPrice() {
            return 10;
        }
    }

    public Pair<Double, Double> generateBill(List<Item> items) {
        double total = 0;
        double meanPrice = 0;

        for(Item item: items) {
            total += item.getPrice();

            if(item.getPrice() > 100) {
                total -= item.getPrice() * 0.1;
            }
        }
        meanPrice = items.size() > 0 ? total / items.size() : 0;

        return new Pair(total, meanPrice);
    }
}
