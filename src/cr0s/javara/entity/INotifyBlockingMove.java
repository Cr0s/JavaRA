package cr0s.javara.entity;

public interface INotifyBlockingMove {
    /**
     * Notify entity that he's blocking path to entity "from"
     * @param from
     */
    public void notifyBlocking(MobileEntity from);
}
