package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

@FunctionalInterface
public interface OutputFormat<T> {
  String format(T t);
}