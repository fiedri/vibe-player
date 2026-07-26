<script>
  let { text, class: extraClass = "" } = $props();

  let containerWidth = $state(0);
  let textWidth = $state(0);

  let isOverflowing = $derived(textWidth > containerWidth);
</script>

<div class="w-full overflow-hidden block" bind:clientWidth={containerWidth}>
  <div class={isOverflowing ? "animate-seamless-marquee flex w-max" : "truncate block"}>
    
    <span bind:clientWidth={textWidth} class="inline-block w-max {extraClass}">
      {text || ""}
    </span>

    {#if isOverflowing}
      <span aria-hidden="true" class="inline-block w-max {extraClass}">
        {text || ""}
      </span>
    {/if}

  </div>
</div>
<style>
@keyframes seamless-marquee {
  0% {
    transform: translateX(0%);
  }
  100% {
    transform: translateX(-50%);   }
}

.animate-seamless-marquee {
  display: flex;
  width: max-content;  white-space: nowrap;
  animation: seamless-marquee 30s linear infinite; }


.animate-seamless-marquee:hover{
  animation-play-state: paused;
}
</style>
