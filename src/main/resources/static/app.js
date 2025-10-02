// app.js

document.addEventListener('DOMContentLoaded', () => {

    // --- LÓGICA COMÚN (MENÚ DE HAMBURGUESA) ---
    const hamburgerMenu = document.getElementById('hamburger-menu');
    const mobileNav = document.getElementById('mobile-nav');
    if (hamburgerMenu && mobileNav) {
        hamburgerMenu.addEventListener('click', () => {
            mobileNav.classList.toggle('show');
        });
    }

    // --- LÓGICA PARA LA PÁGINA DE REGISTRO ---
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const nombre = event.target.nombre.value;
            const email = event.target.email.value;
            const password = event.target.password.value;

            try {
                const response = await fetch('/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ nombre, email, password })
                });

                if (response.ok) {
                    alert('¡Registro exitoso! Ahora puedes iniciar sesión.');
                    window.location.href = 'login.html';
                } else {
                    const errorData = await response.json();
                    alert(`Error en el registro: ${errorData.message || 'Por favor, revisa tus datos.'}`);
                }
            } catch (error) {
                console.error('Error de red al intentar registrar:', error);
                alert('No se pudo conectar con el servidor. Inténtalo más tarde.');
            }
        });
    }

    // --- LÓGICA PARA LA PÁGINA DE LOGIN ---
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const email = event.target.email.value;
            const password = event.target.password.value;

            try {
                const response = await fetch('/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (response.ok) {
                    const data = await response.json();
                    const token = data.token || data.accessToken;
                    if (token) {
                        localStorage.setItem('jwt_token', token);
                        window.location.href = 'index.html';
                    } else {
                        alert('Error: No se recibió un token del servidor.');
                    }
                } else {
                    alert('Error: Email o contraseña incorrectos.');
                }
            } catch (error) {
                console.error('Error de red al intentar iniciar sesión:', error);
                alert('No se pudo conectar con el servidor. Inténtalo más tarde.');
            }
        });
    }

    // --- LÓGICA PARA LA PÁGINA DE TAREAS (INDEX.HTML) ---
    const taskListContainer = document.getElementById('task-list');
    if (taskListContainer) {
        const token = localStorage.getItem('jwt_token');
        if (!token) {
            window.location.href = 'login.html';
            return;
        }

        const createTaskForm = document.getElementById('create-task-form');

        // Función para OBTENER y RENDERIZAR las tareas desde la API
        const fetchAndRenderTasks = async () => {
            try {
                const response = await fetch('/api/tareas', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (!response.ok) {
                    if(response.status === 403) {
                         alert('Tu sesión ha expirado. Por favor, inicia sesión de nuevo.');
                         localStorage.removeItem('jwt_token');
                         window.location.href = 'login.html';
                    }
                    throw new Error('No se pudieron cargar las tareas.');
                }

                const page = await response.json();
                const tasks = page.content; // La API devuelve una página, las tareas están en "content"
                renderTasks(tasks);

            } catch (error) {
                console.error('Error al obtener tareas:', error);
                taskListContainer.innerHTML = `<div class="empty-list-message"><p>Error al cargar las tareas.</p></div>`;
            }
        };

        // Función para DIBUJAR las tareas en el HTML
        const renderTasks = (tasks) => {
            taskListContainer.innerHTML = ''; // Limpia la lista
            if (tasks.length === 0) {
                taskListContainer.innerHTML = `<div class="empty-list-message"><h4>¡Felicidades!</h4><p>No tienes tareas pendientes. Puedes añadir una nueva usando el formulario de arriba.</p></div>`;
                return;
            }

            tasks.forEach(task => {
                const taskElement = document.createElement('div');
                taskElement.className = 'task-item';
                taskElement.dataset.id = task.id;
                taskElement.dataset.priority = task.prioridad;

                taskElement.innerHTML = `
                    <div class="task-item-header"><h4>${task.titulo}</h4></div>
                    <div class="task-item-body"><p>${task.descripcion || ''}</p></div>
                    <div class="task-item-footer">
                        <span class="task-status">${task.estado.replace('_', ' ')}</span>
                        <div class="task-actions">
                            <button class="btn-edit" title="Editar"><i class="fas fa-pencil-alt"></i></button>
                            <button class="btn-delete" title="Eliminar"><i class="fas fa-trash-alt"></i></button>
                        </div>
                    </div>
                `;
                taskListContainer.appendChild(taskElement);
            });

            // Añadir event listeners para los botones de eliminar
            document.querySelectorAll('.btn-delete').forEach(button => {
                button.addEventListener('click', (e) => {
                    const taskId = e.target.closest('.task-item').dataset.id;
                    handleDeleteTask(taskId);
                });
            });
        };

        // Función para MANEJAR LA ELIMINACIÓN de una tarea
        const handleDeleteTask = async (taskId) => {
            if (!confirm('¿Estás seguro de que quieres eliminar esta tarea?')) return;

            try {
                const response = await fetch(`/api/tareas/${taskId}`, {
                    method: 'DELETE',
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (response.ok) {
                    fetchAndRenderTasks(); // Recarga la lista de tareas
                } else if (response.status === 403) {
                    alert('Error al eliminar: No tienes permisos de Administrador para realizar esta acción.');
                } else {
                    alert('Error al eliminar la tarea.');
                }
            } catch (error) {
                console.error('Error de red al eliminar tarea:', error);
            }
        };

        // Evento para CREAR una nueva tarea
        createTaskForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const titulo = document.getElementById('task-title').value;
            const descripcion = document.getElementById('task-desc').value;
            const prioridad = document.getElementById('task-priority').value;
            // El estado no se envía, el backend lo asigna por defecto

            try {
                const response = await fetch('/api/tareas', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({ titulo, descripcion, prioridad })
                });

                if (response.ok) {
                    fetchAndRenderTasks(); // Recarga la lista de tareas
                    createTaskForm.reset(); // Limpia el formulario
                } else {
                    const errorData = await response.json();
                    alert(`Error al crear la tarea: ${errorData.message || 'Revisa los datos.'}`);
                }
            } catch (error) {
                console.error('Error de red al crear tarea:', error);
            }
        });
        
        // Lógica para el botón de cerrar sesión
        const logoutButton = document.querySelector('a[href="login.html"]');
        if(logoutButton) {
            logoutButton.addEventListener('click', (e) => {
                e.preventDefault();
                localStorage.removeItem('jwt_token');
                window.location.href = 'login.html';
            });
        }

        // Carga inicial de las tareas al entrar a la página
        fetchAndRenderTasks();
    }
});
