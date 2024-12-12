package org.iesvdm.jsp_servlet_jdbc.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.iesvdm.jsp_servlet_jdbc.dao.SocioDAO;
import org.iesvdm.jsp_servlet_jdbc.dao.SocioDAOImpl;
import org.iesvdm.jsp_servlet_jdbc.model.Socio;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "/EditarSociosServlet", value = "/EditarSociosServlet")
public class EditarSociosServlet extends HttpServlet {

    /***
     * Haremos el GET
     * del formulario de edición de socios y POST para actualizar los datos del socio
     *
     * Cuando se actualizan los datos en base de datos (POST) correctamente
     * habrá una carga del listado principial de pideNumeroSocio.jsp.
     *
     *  Cuando se produzca algún error de validación,
     *  se carga el formulario de edición informando del error.
     *
     *  El formulario de edición tendrás que crearlo. Para ello,
     *  inspírate en formularioSocio.jsp, pero, en este caso, llámalo formularioEditarSocio.jsp
     **/

    private SocioDAO socioDAO = new SocioDAOImpl();


    //metodo doGet obtenemos el socio a editar y lo enviamos al formulario de edición
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Recogemos el codigo del socio a editar
        int nsocio = Integer.parseInt(request.getParameter("codigo"));

        // El find de la interfaz SocioDAO nos devuelve un Optional de Socio
        // Si el socio existe lo editamos y sino damos un mensaje de error
        Optional<Socio> socioOptional = this.socioDAO.find(nsocio);

        if (socioOptional.isPresent()) {
            Socio editarSocio = socioOptional.get();
            // Preparamos el socio para enviarlo al formulario de edición
            request.setAttribute("editarSocio", editarSocio);
        } else {
            // Si el optional no está presente, es decir, no existe el socio con ese codigo
            // Entonces redirigimos a la página de listado de socios con un mensaje de error
            request.setAttribute("error", "No existe el socio con el codigo: " + nsocio);
            request.getRequestDispatcher("/WEB-INF/jsp/listadoSociosB.jsp").forward(request, response);
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/formularioEditarSocio.jsp");
        dispatcher.forward(request, response);
    }


    //metodo doPost actualizamos los datos del socio
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Creamos un objeto RequestDispatcher para luego redirigir a la página de listado de socios
        RequestDispatcher dispatcher = null;

        // Recuperamos el id del socio a editar y lo validamos

        int numeroSocio = -1;
        try {
            numeroSocio = Integer.parseInt(request.getParameter("codigo"));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "ID Socio no válido");
            dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/formularioEditarSocio.jsp");
            dispatcher.forward(request, response);
            return;
        }


        //verificanos que el socio con ese ID existe
        Optional<Socio> socioOptional = this.socioDAO.find(numeroSocio);
        if (!socioOptional.isPresent()) {
            request.setAttribute("error", "No existe el socio con el codigo: " + numeroSocio);
            dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/formularioEditarSocio.jsp");
            dispatcher.forward(request, response);
            return;
        }

        //validamos los datos del socio
        Optional<Socio> socioValidado = UtilServlet.validaGrabar(request);
        if (socioValidado.isPresent()) {
            Socio editarSocio = socioValidado.get();
            editarSocio.setSocioId(numeroSocio);

            //actualizamos los datos del socio
            this.socioDAO.update(editarSocio);

            // Redirigimos a la página de listado
            List<Socio> listado = this.socioDAO.getAll();
            request.setAttribute("listado", listado);
            request.getRequestDispatcher("/WEB-INF/jsp/listadoSociosB.jsp").forward(request, response);
        } else {
            //si hay errores en la validación
            request.setAttribute("error", "Error en la validación de los datos, Inténtelo de nuevo");
            request.setAttribute("editarSocio", socioOptional.get()); //volvemos a enviar el socio a editar al formulario
            request.getRequestDispatcher("/WEB-INF/jsp/formularioEditarSocio.jsp").forward(request, response);
        }
    }
}