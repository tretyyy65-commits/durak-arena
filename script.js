const playButton = document.getElementById("playButton");

playButton.addEventListener("click", () => {
});

const menuButtons = document.querySelectorAll(".quick-menu button");

menuButtons.forEach((button) => {
  button.addEventListener("click", () => {
    const name = button.querySelector("span").textContent;

  });
});

const bottomButtons = document.querySelectorAll(".bottom-nav button");

bottomButtons.forEach((button) => {
  button.addEventListener("click", () => {

    bottomButtons.forEach((item) => {
      item.classList.remove("active");
    });

    button.classList.add("active");

    const name = button.querySelector("span").textContent;

    if (name !== "Головна") {
    }
  });
});