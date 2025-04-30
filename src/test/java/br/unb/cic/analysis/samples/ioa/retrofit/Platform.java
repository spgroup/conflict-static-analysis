package br.unb.cic.analysis.samples.ioa.retrofit;

abstract class Platform {
    private static final Platform PLATFORM = findPlatform();

    static Platform get() {
        return PLATFORM;
    }

    private static Platform findPlatform() {
        try {
            Class.forName("android.os.Build");
            return new Android();
        } catch (ClassNotFoundException ignored) {
        }
        return new Base();
    }

    abstract RestAdapter.Log defaultLog();

    private static class Android extends Platform {

        @Override
        RestAdapter.Log defaultLog() {
            return new AndroidLog("Retrofit");
        }
    }

    private static class Base extends Platform {
        @Override
        RestAdapter.Log defaultLog() {
            return new RestAdapter.Log() {
                @Override
                public void log(String message) {
                    System.out.println(message);
                }
            };
        }
    }
}
