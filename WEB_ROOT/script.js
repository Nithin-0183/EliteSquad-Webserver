document.addEventListener('DOMContentLoaded', function () {
    loadMessages();

    document.getElementById('chatForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const user = document.getElementById('user').value;
        const message = document.getElementById('message').value;

        fetch('/messages', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `user=${encodeURIComponent(user)}&message=${encodeURIComponent(message)}`
        }).then(response => {
            if (response.ok) {
                loadMessages();
                document.getElementById('message').value = '';
            }
        });
    });
});

function loadMessages() {
    fetch('/messages')
        .then(response => response.json())
        .then(messages => {
            const chat = document.getElementById('chat');
            chat.innerHTML = '';
            messages.reverse().forEach(msg => {
                const messageElement = document.createElement('div');
                messageElement.classList.add('border', 'p-2', 'mb-2');
                messageElement.setAttribute('data-id', msg.id);
                messageElement.innerHTML = `
                    <strong>${msg.user}</strong> <small>${msg.timestamp}</small>
                    <p>${msg.message}</p>
                    <button class="btn btn-sm btn-warning edit-button">Edit</button>
                    <button class="btn btn-sm btn-danger delete-button">Delete</button>
                `;
                chat.appendChild(messageElement);
            });

            document.querySelectorAll('.edit-button').forEach(button => {
                button.addEventListener('click', function () {
                    const messageElement = this.parentElement;
                    const id = messageElement.getAttribute('data-id');
                    const oldMessage = messageElement.querySelector('p').innerText;
                    editMessage(id, oldMessage);
                });
            });

            document.querySelectorAll('.delete-button').forEach(button => {
                button.addEventListener('click', function () {
                    const messageElement = this.parentElement;
                    const id = messageElement.getAttribute('data-id');
                    deleteMessage(id);
                });
            });
        });
}

function editMessage(id, oldMessage) {
    const newMessage = prompt('Edit your message:', oldMessage);
    if (newMessage && newMessage !== oldMessage) {
        fetch(`/messages/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `message=${encodeURIComponent(newMessage)}`
        }).then(response => {
            if (response.ok) {
                loadMessages();
            }
        });
    }
}

function deleteMessage(id) {
    if (confirm('Are you sure you want to delete this message?')) {
        fetch(`/messages/${id}`, {
            method: 'DELETE'
        }).then(response => {
            if (response.ok) {
                loadMessages();
            }
        });
    }
}
