const screens=[...document.querySelectorAll('.screen')];
const navButtons=[...document.querySelectorAll('[data-go]')];
const loadBar=document.getElementById('loadBar');
const loadText=document.getElementById('loadText');

function showScreen(name){
  screens.forEach(screen=>screen.classList.toggle('active',screen.dataset.screen===name));
  document.querySelectorAll('.bottom-nav button').forEach(btn=>btn.classList.toggle('active',btn.dataset.go===name));
  window.scrollTo({top:0,behavior:'instant'});
}

function startLoading(){
  let progress=0;
  const timer=setInterval(()=>{
    progress=Math.min(100,progress+Math.floor(Math.random()*9)+5);
    if(loadBar) loadBar.style.width=`${progress}%`;
    if(loadText) loadText.textContent=`Підготовка арени… ${progress}%`;
    if(progress>=100){
      clearInterval(timer);
      setTimeout(()=>showScreen('auth'),260);
    }
  },110);
}

navButtons.forEach(button=>{
  button.addEventListener('click',()=>{
    const target=button.dataset.go;
    if(target) showScreen(target);
  });
});

document.getElementById('googleLogin')?.addEventListener('click',()=>{
  // Тут підключається реальний Firebase / Google Sign-In.
  showScreen('home');
});

document.getElementById('guestLogin')?.addEventListener('click',()=>showScreen('home'));

document.querySelectorAll('.mode-card:not(.locked-card)').forEach(card=>{
  card.addEventListener('click',()=>{
    card.animate([
      {transform:'scale(1)'},
      {transform:'scale(.985)'},
      {transform:'scale(1)'}
    ],{duration:180,easing:'ease-out'});
  });
});

startLoading();
