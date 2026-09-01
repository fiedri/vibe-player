import { paraglideVitePlugin } from "@inlang/paraglide-js";
import tailwindcss from "@tailwindcss/vite";
import postcssPresetEnv from "postcss-preset-env";
import adapter from "@sveltejs/adapter-static";
import { sveltekit } from "@sveltejs/kit/vite";
import { vitePreprocess } from "@sveltejs/vite-plugin-svelte";
import { defineConfig } from "vite";
import { execSync } from "node:child_process";

/**
 * Deterministic build id for SvelteKit versioning.
 * Reads from env, else derives from the current git commit hash —
 * identical value on every machine for the same commit (reproducible build).
 */
function getVersion(): string {
	try {
		return process.env.BUILD_ID ?? execSync("git rev-parse HEAD", { encoding: "utf-8" }).trim();
	} catch {
		return "0";
	}
}

export default defineConfig({
	build: {
		sourcemap: false,
		// Transpilar el bundle JS a ES2018: evita operadores ES2021 (??=, &&=)
		// que rompen en System WebView < 85 (emuladores y teléfonos antiguos).
		target: "es2020"
	},
	css: {
		postcss: { plugins: [postcssPresetEnv({ browsers: "Chrome >= 85" })] }
	},
	plugins: [
		tailwindcss(),
		sveltekit({
			preprocess: vitePreprocess(),
			compilerOptions: {
				runes: ({ filename }) => filename.split(/[/\\]/).includes("node_modules") ? undefined : true
			},

			adapter: adapter({
				pages: "build",
				assets: "build",
				fallback: "index.html",
				precompress: false,
				strict: true
			}),

			typescript: {
				config: (config) => {
					config.include.push("../drizzle.config.ts");
				}
			},
			version: { name: getVersion(), pollInterval: 0 },

		}),

		paraglideVitePlugin({
			project: "./project.inlang",
			outdir: "./src/lib/paraglide",
			emitTsDeclarations: true
		})
	]
});
