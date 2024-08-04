function initFormHandler() {
    document.getElementById('add-server-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);

        fetch('/admin/add-server', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            alert(`${data.message}. Domain: ${data.domain}`);
        })
        .catch(error => console.error('Error:', error));
    });
}
