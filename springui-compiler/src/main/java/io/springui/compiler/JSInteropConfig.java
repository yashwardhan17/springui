package io.springui.compiler;

/**
 * JSInteropConfig — configuration for JS glue file generation.
 */
public class JSInteropConfig {

    private final String componentName;
    private final String wasmFileName;
    private final String mountElementId;
    private final String frameworkVersion;

    private JSInteropConfig(Builder builder) {
        this.componentName = builder.componentName;
        this.wasmFileName = builder.wasmFileName;
        this.mountElementId = builder.mountElementId;
        this.frameworkVersion = builder.frameworkVersion;
    }

    public String getComponentName() { return componentName; }
    public String getWasmFileName() { return wasmFileName; }
    public String getMountElementId() { return mountElementId; }
    public String getFrameworkVersion() { return frameworkVersion; }

    public static class Builder {
        private String componentName = "App";
        private String wasmFileName = "app.wasm";
        private String mountElementId = "root";
        private String frameworkVersion = "0.1.0-alpha";

        public Builder componentName(String name) {
            this.componentName = name;
            return this;
        }

        public Builder wasmFileName(String fileName) {
            this.wasmFileName = fileName;
            return this;
        }

        public Builder mountElementId(String id) {
            this.mountElementId = id;
            return this;
        }

        public Builder frameworkVersion(String version) {
            this.frameworkVersion = version;
            return this;
        }

        public JSInteropConfig build() {
            return new JSInteropConfig(this);
        }
    }
}