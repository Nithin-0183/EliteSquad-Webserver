document.querySelectorAll('.sidebar a').forEach(link => {
    link.addEventListener('click', function(event) {
        event.preventDefault(); // Prevent the default anchor behavior
        document.querySelectorAll('.sidebar a').forEach(l => l.classList.remove('active'));
        this.classList.add('active');
        
        document.querySelectorAll('.main-content > div').forEach(div => {
            div.style.display = 'none';
        });
        
        const section = document.querySelector(this.getAttribute('href'));
        if (section) {
            section.style.display = 'block';
        }
    });
});
