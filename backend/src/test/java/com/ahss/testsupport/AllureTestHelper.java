package com.ahss.testsupport;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public final class AllureTestHelper {
    private AllureTestHelper() {
    }

    // Reflection-based facade to avoid hard dependency on Allure at compile time
    private static final class AllureFacade {
        private static final Class<?> ALLURE_CLASS;
        private static final Class<?> OPTIONAL_CLASS;
        private static final boolean SUPPRESS;
        static {
            Class<?> c;
            try {
                c = Class.forName("io.qameta.allure.Allure");
            } catch (Throwable t) {
                c = null;
            }
            ALLURE_CLASS = c;
            Class<?> o;
            try {
                o = Class.forName("java.util.Optional");
            } catch (Throwable t) {
                o = null;
            }
            OPTIONAL_CLASS = o;
            // Allow opting-out of Allure calls to avoid noisy lifecycle errors
            String suppressProp = System.getProperty("allure.suppress", "true");
            SUPPRESS = "true".equalsIgnoreCase(suppressProp);
        }

        private static Method find(String name, Class<?>... types) {
            if (ALLURE_CLASS == null)
                return null;
            try {
                return ALLURE_CLASS.getMethod(name, types);
            } catch (Throwable t) {
                return null;
            }
        }

        private static Object getLifecycle() {
            Method m = find("getLifecycle");
            if (m == null)
                return null;
            try {
                return m.invoke(null);
            } catch (Throwable t) {
                return null;
            }
        }

        private static boolean isLifecycleReady() {
            if (SUPPRESS)
                return false;
            // If Allure is absent, treat as not ready (we will noop)
            if (ALLURE_CLASS == null)
                return false;
            Object lifecycle = getLifecycle();
            if (lifecycle == null)
                return false;
            Class<?> lc = lifecycle.getClass();
            // Try common methods to detect current test case/step
            String[] candidates = new String[] {
                    "getCurrentTestCase",
                    "getCurrentTestCaseOrStep",
                    "getCurrentStep"
            };
            for (String methodName : candidates) {
                try {
                    Method m = lc.getMethod(methodName);
                    Object result = m.invoke(lifecycle);
                    if (result == null)
                        continue;
                    // Optional handling
                    if (OPTIONAL_CLASS != null && OPTIONAL_CLASS.isInstance(result)) {
                        try {
                            Method isPresent = OPTIONAL_CLASS.getMethod("isPresent");
                            Object present = isPresent.invoke(result);
                            if (present instanceof Boolean) {
                                return (Boolean) present;
                            }
                        } catch (Throwable ignored) {
                            // If we cannot check, consider not ready
                            return false;
                        }
                    }
                    // Fallback: non-null result means some context exists
                    return true;
                } catch (Throwable ignored) {
                    // Try next candidate
                }
            }
            // If no detection method found, consider not ready
            return false;
        }

        static void step(String name, Runnable runnable) {
            Method m = find("step", String.class, Runnable.class);
            if (m != null) {
                try {
                    // Guard: if lifecycle not ready, just run without invoking Allure
                    if (!isLifecycleReady()) {
                        runnable.run();
                        return;
                    }
                    m.invoke(null, name, runnable);
                    return;
                } catch (Throwable ignored) {
                    /* fall through */ }
            }
            // Fallback: just run
            runnable.run();
        }

        static void addAttachment(String name, String type, String content) {
            Method m = find("addAttachment", String.class, String.class, String.class);
            if (m != null) {
                try {
                    if (!isLifecycleReady())
                        return; // avoid AllureLifecycle errors
                    m.invoke(null, name, type, content);
                } catch (Throwable ignored) {
                    /* noop */ }
            }
        }

        static void addAttachment(String name, String type, ByteArrayInputStream stream, String ext) {
            Method m = find("addAttachment", String.class, String.class, java.io.InputStream.class, String.class);
            if (m != null) {
                try {
                    if (!isLifecycleReady())
                        return; // avoid AllureLifecycle errors
                    m.invoke(null, name, type, stream, ext);
                } catch (Throwable ignored) {
                    /* noop */ }
            }
        }

        static void label(String name, String value) {
            Method m = find("label", String.class, String.class);
            if (m != null) {
                try {
                    if (!isLifecycleReady())
                        return; // avoid AllureLifecycle errors
                    m.invoke(null, name, value);
                } catch (Throwable ignored) {
                    /* noop */ }
            }
        }

        static void description(String text) {
            Method m = find("description", String.class);
            if (m != null) {
                try {
                    if (!isLifecycleReady())
                        return;
                    m.invoke(null, text);
                } catch (Throwable ignored) {
                    /* noop */ }
            }
        }

        static void descriptionHtml(String html) {
            Method m = find("descriptionHtml", String.class);
            if (m != null) {
                try {
                    if (!isLifecycleReady())
                        return;
                    m.invoke(null, html);
                } catch (Throwable ignored) {
                    /* noop */ }
            }
        }

        static void link(String name, String url) {
            Method m = find("link", String.class, String.class);
            if (m != null) {
                try {
                    if (!isLifecycleReady())
                        return;
                    m.invoke(null, name, url);
                } catch (Throwable ignored) {
                    /* noop */ }
            }
        }
    }

    public static void step(String name, Runnable runnable) {
        AllureFacade.step(name, runnable);
    }

    public static <T> T step(String name, Supplier<T> supplier) {
        final var holder = new Object() {
            T value;
        };
        AllureFacade.step(name, () -> holder.value = supplier.get());
        return holder.value;
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    public static void stepUnchecked(String name, CheckedRunnable runnable) {
        AllureFacade.step(name, () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> T stepUnchecked(String name, CheckedSupplier<T> supplier) {
        final var holder = new Object() {
            T value;
        };
        AllureFacade.step(name, () -> {
            try {
                holder.value = supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return holder.value;
    }

    public static void attachText(String name, String content) {
        AllureFacade.addAttachment(name, "text/plain", content);
    }

    public static void attachJson(String name, String json) {
        AllureFacade.addAttachment(name, "application/json", json);
    }

    public static void attachBytes(String name, byte[] bytes, String type, String fileExtension) {
        AllureFacade.addAttachment(name, type, new ByteArrayInputStream(bytes), fileExtension);
    }

    public static void labelEpic(String epic) {
        AllureFacade.label("epic", epic);
    }

    public static void labelFeature(String feature) {
        AllureFacade.label("feature", feature);
    }

    public static void labelStory(String story) {
        AllureFacade.label("story", story);
    }

    public static void labelOwner(String owner) {
        AllureFacade.label("owner", owner);
    }

    // Generic label support to avoid importing Allure types in tests
    public static void label(String name, String value) {
        AllureFacade.label(name, value);
    }

    public static void labelSeverity(String severityValue) {
        AllureFacade.label("severity", severityValue);
    }

    // Description and links helpers to avoid annotation imports in tests
    public static void description(String text) {
        AllureFacade.description(text);
    }

    public static void descriptionHtml(String html) {
        AllureFacade.descriptionHtml(html);
    }

    public static void link(String name, String url) {
        AllureFacade.link(name, url);
    }

    public static void issue(String id) {
        // Store issue id as label
        AllureFacade.label("issue", id);
    }

    public static void issue(String name, String url) {
        AllureFacade.link(name, url);
        AllureFacade.label("issue", name);
    }

    public static void tms(String id) {
        AllureFacade.label("tms", id);
    }

    public static void tms(String name, String url) {
        AllureFacade.link(name, url);
        AllureFacade.label("tms", name);
    }
}