import tailwindcss from "@tailwindcss/vite";
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
	},
	plugins: [
		tailwindcss(),
		sveltekit({
			preprocess: vitePreprocess(),
			compilerOptions: {
				runes: ({ filename }) =>
					filename.split(/[/\\]/).includes("node_modules") ? undefined : true
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
			// kit.version.name — MUST live here (in vite.config) because passing
			// options to sveltekit() makes SvelteKit ignore svelte.config.ts entirely.
			version: {
				name: getVersion(),
				pollInterval: 0
			}
		})
	]
});
