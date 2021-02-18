package com.destrostudios.turnbasedgametools.grid;

class PriorityItem<T, P extends Comparable<P>> implements Comparable<PriorityItem<T, P>> {
    public final T item;
    public final P priority;

    public PriorityItem(T item, P priority) {
        this.item = item;
        this.priority = priority;
    }

    @Override
    public int compareTo(PriorityItem<T, P> o) {
        return priority.compareTo(o.priority);
    }
}
