package samba.exceptions;

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionUtil {

  @SuppressWarnings("unchecked")
  public static <T extends Throwable> Optional<T> getCause(
      final Throwable err, final Class<? extends T> targetType) {
    return ExceptionUtils.getThrowableList(err).stream()
        .filter(targetType::isInstance)
        .map(e -> (T) e)
        .findFirst();
  }

  public static <T extends Throwable> boolean hasCause(
      final Throwable err, final Class<? extends T> targetType) {
    return getCause(err, targetType).isPresent();
  }

  public static String getRootCauseMessage(final Throwable err) {
    return Optional.ofNullable(ExceptionUtils.getRootCause(err))
        .map(ExceptionUtil::getMessageOrSimpleName)
        .orElse("");
  }

  @SafeVarargs
  public static boolean hasCause(
      final Throwable err, final Class<? extends Throwable>... targetTypes) {

    return ExceptionUtils.getThrowableList(err).stream()
        .anyMatch(cause -> isAnyOf(cause, targetTypes));
  }

  @SafeVarargs
  private static boolean isAnyOf(
      final Throwable cause, final Class<? extends Throwable>... targetTypes) {
    for (Class<? extends Throwable> targetType : targetTypes) {
      if (targetType.isInstance(cause)) {
        return true;
      }
    }
    return false;
  }

  public static String getMessageOrSimpleName(final Throwable throwable) {
    return Optional.ofNullable(throwable.getMessage()).orElse(throwable.getClass().getSimpleName());
  }
}
