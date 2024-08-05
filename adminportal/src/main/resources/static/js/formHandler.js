function initFormHandler() {
    document.getElementById('add-server-form').addEventListener('submit', (e) => {
        e.preventDefault();

        const domainInput = document.getElementById('domain');
        const domainValue = domainInput.value;

        // Check if the domain is unique
        fetch(`/admin/check-domain?domain=${encodeURIComponent(domainValue)}`)
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    alert('This domain name is already taken. Please choose another one.');
                    domainInput.focus();
                } else {
                    // Domain is unique, proceed with form submission
                    const formData = new FormData(e.target);

                    fetch('/admin/add-server', {
                        method: 'POST',
                        body: formData
                    })
                    .then(response => response.json())
                    .then(data => {
                        alert(`${data.message}. Domain: ${data.domain}`);
                        
                        // Close the popup
                        const popupElement = window.frameElement.closest('.popup'); 
                        if (popupElement) {
                            popupElement.style.display = 'none';
                        } else {
                            console.error('Popup element not found.');
                        }
                        
                        // Reload the server list in the parent window
                        if (window.parent && window.parent.loadServers) {
                            window.parent.loadServers(); // Call the parent's loadServers function
                        } else {
                            console.error('Parent window or loadServers function not found.');
                        }

                        // Reset the form fields after successful submission
                        e.target.reset();
                    })
                    .catch(error => console.error('Error:', error));
                }
            })
            .catch(error => console.error('Error checking domain:', error));
    });
}
