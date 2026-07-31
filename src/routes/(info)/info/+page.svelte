<script lang="ts">
  import { TriangleAlert } from "@lucide/svelte";
</script>

<div class="h-full w-full min-w-0 overflow-y-auto">
  <div class="flex flex-col gap-6 p-5 pb-10">
    <p class="text-sm leading-relaxed text-muted-foreground">
      Vibe usa un <span class="font-medium text-white">servicio en primer plano</span>
      (foreground service) para que la música siga sonando aunque dejes la app en
      segundo plano. En Android stock esto funciona sin que tengas que tocar nada.
      Sin embargo, algunas marcas —Xiaomi, Samsung, OnePlus, Huawei— traen
      optimizadores de batería tan agresivos que pueden cerrar Vibe. Acá te mostramos
      cómo permitirla en cada una.
    </p>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>Android genérico</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          Ajustes → Apps → Vibe → Batería → seleccioná
          <strong class="font-medium text-white">"Sin restricciones"</strong>
          (o "No optimizar").
        </li>
        <li>
          Alternativa: Ajustes → Batería → Optimización de batería → buscá Vibe y
          desactivá la optimización.
        </li>
      </ol>
    </section>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>Xiaomi / MIUI / HyperOS</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          Ajustes → Apps → Permisos de apps → Inicio automático en segundo plano →
          permití Vibe.
        </li>
        <li>
          Ajustes → Batería → seleccioná
          <strong class="font-medium text-white">"Sin restricciones"</strong> para Vibe.
        </li>
        <li>
          En Opciones de desarrollador → desactivá la
          <strong class="font-medium text-white">"Optimización MIUI"</strong>.
        </li>
      </ol>
    </section>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>Samsung (One UI)</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          Ajustes → Batería → Límites de uso en segundo plano → Apps que nunca
          duermen → agregá Vibe.
        </li>
      </ol>
    </section>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>OnePlus / OPPO (ColorOS)</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          Ajustes → Batería → desactivá
          <strong class="font-medium text-white">"Batería adaptable"</strong> y/o
          <strong class="font-medium text-white">"Optimización profunda"</strong>.
        </li>
        <li>Habilitá el Auto-inicio de Vibe.</li>
        <li>
          Fijá Vibe en el multitarea: abrí los Recents y bloqueá la tarjeta de Vibe
          (mantenela presionada y tocá el candado).
        </li>
      </ol>
    </section>

    <section
      class="bg-card p-4 border border-border [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
    >
      <h2>Huawei (EMUI / HarmonyOS)</h2>
      <ol class="list-inside list-decimal space-y-2 text-sm text-muted-foreground marker:text-primary">
        <li>
          Ajustes → Batería → Inicio de apps → Administrar manualmente → permití
          Vibe (activá "Permitir inicio automático", "Permitir actividad en segundo
          plano" y "Permitir ejecución en segundo plano").
        </li>
      </ol>
    </section>

    <div
      class="flex items-start gap-3 p-4 border border-destructive/30 bg-destructive/10 text-sm text-destructive"
    >
      <TriangleAlert class="mt-0.5 size-4 shrink-0" />
      <p>
        En OnePlus y otras marcas, la configuración puede revertirse con las
        actualizaciones del sistema. Si Vibe deja de sonar en segundo plano, volvé a
        revisar estos pasos.
      </p>
    </div>
  </div>
</div>
