<script lang="ts">
  import {WarningAlt as TriangleAlert} from "carbon-icons-svelte";
  import { m } from "$lib/paraglide/messages.js";
</script>

<div class="h-full w-full min-w-0 overflow-y-auto">
  <div class="flex flex-col gap-6 p-5 pb-10">
    <p class="text-sm leading-relaxed text-muted-foreground">
      {m["info_page.intro_before"]()}
      <span class="font-medium text-white">{m["info_page.foreground_service"]()}</span>
      {m["info_page.intro_after"]()}
    </p>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>{m["info_page.generic.title"]()}</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          {m["info_page.generic.step_1_before"]()}
          <strong class="font-medium text-white">{m["info_page.generic.step_1_option"]()}</strong>
          {m["info_page.generic.step_1_after"]()}
        </li>
        <li>
          {m["info_page.generic.step_2"]()}
        </li>
      </ol>
    </section>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>{m["info_page.xiaomi.title"]()}</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          {m["info_page.xiaomi.step_1"]()}
        </li>
        <li>
          {m["info_page.xiaomi.step_2_before"]()}
          <strong class="font-medium text-white">{m["info_page.xiaomi.step_2_option"]()}</strong>
          {m["info_page.xiaomi.step_2_after"]()}
        </li>
        <li>
          {m["info_page.xiaomi.step_3_before"]()}
          <strong class="font-medium text-white">{m["info_page.xiaomi.step_3_option"]()}</strong>
          {m["info_page.xiaomi.step_3_after"]()}
        </li>
      </ol>
    </section>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>{m["info_page.samsung.title"]()}</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          {m["info_page.samsung.step_1"]()}
        </li>
      </ol>
    </section>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>{m["info_page.oneplus.title"]()}</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          {m["info_page.oneplus.step_1_before"]()}
          <strong class="font-medium text-white">{m["info_page.oneplus.step_1_option_a"]()}</strong>
          {m["info_page.oneplus.step_1_middle"]()}
          <strong class="font-medium text-white">{m["info_page.oneplus.step_1_option_b"]()}</strong>
          {m["info_page.oneplus.step_1_after"]()}
        </li>
        <li>{m["info_page.oneplus.step_2"]()}</li>
        <li>
          {m["info_page.oneplus.step_3"]()}
        </li>
      </ol>
    </section>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>{m["info_page.huawei.title"]()}</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          {m["info_page.huawei.step_1"]()}
        </li>
      </ol>
    </section>

    <div
      class="flex items-start gap-3 p-4 border border-red-950 bg-red-300 text-sm text-red-950"
    >
      <TriangleAlert class="mt-0.5 size-4 shrink-0" />
      <p>
        {m["info_page.warning"]()}
      </p>
    </div>
  </div>
</div>
