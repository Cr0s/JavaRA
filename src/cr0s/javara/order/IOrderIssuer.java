package cr0s.javara.order;

import java.util.ArrayList;

import cr0s.javara.entity.Entity;

public interface IOrderIssuer {
    public ArrayList<OrderTargeter> getOrders();
    public Order issueOrder(Entity self, OrderTargeter targeter, Target target, InputAttributes ia);
}
