package com.cn.ey.demo.support.common;

public abstract class AbstractChainHandler<T, R> {
    private AbstractChainHandler<T, R> next;

    private AbstractChainHandler() {}

    public abstract R execute(Chain<T, R> chain);

    public static class Chain<T, R> {
        T data;

        AbstractChainHandler<T, R> curr;

        public T data() { return data; }

        public R proceed(T data) {
            R result = null;
            if (curr != null) {
                result = curr.execute(new Chain<>(data, curr.next));
            }
            return result;
        }

        private Chain(T data, AbstractChainHandler<T, R> curr) {
            this.data = data;
            this.curr = curr;
        }
    }

    public static class Builder<T, R> {
        AbstractChainHandler<T, R> head;
        AbstractChainHandler<T, R> curr;

        public Builder<T, R> add(AbstractChainHandler<T, R> curr) {
            if (this.head == null) {
                this.head = curr;
            }
            if (this.curr != null) {
                this.curr.next = curr;
            }
            this.curr = curr;
            return this;
        }

        public Chain<T, R> build() {
            return new Chain<>(null, head);
        }
    }


    public static void main(String[] args) {
        class Data {
            public String noop;
            public Data(String noop) { this.noop = noop; }
        }
        class Student extends AbstractChainHandler<Data, String> {
            @Override
            public String execute(AbstractChainHandler.Chain<Data, String> chain) {
                chain.data().noop += ", Im student - first call";
                System.out.println(chain.data().noop);
                return chain.proceed(chain.data());
            }
        }
        class Teacher extends AbstractChainHandler<Data, String> {
            @Override
            public String execute(AbstractChainHandler.Chain<Data, String> chain) {
                chain.data().noop += ", Im teacher - second call";
                System.out.println(chain.data().noop);
                return chain.data().noop + ", finish";
            }
        }

        // build chain
        AbstractChainHandler.Builder<Data, String> builder = new AbstractChainHandler.Builder<>();
        String result = builder.add(new Student()).add(new Teacher()).build().proceed(new Data("Jack"));
        System.out.println(result);
    }

}
