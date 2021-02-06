package controllers;

import businessLogic.PlanningUnitHelper;
import com.typesafe.config.Config;
import enumerations.SchedulingState;
import exceptions.NoSuchIDException;
import exceptions.UnauthorizedException;
import language.TranslateService;
import models.Member;
import models.planning.PlanningMember;
import play.mvc.Controller;
import utilityClasses.*;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/*
 * MailController is not really a controller, it does not answer API calls. So it does not extend Controller.
 * It interacts with the employees over mail, so the name Controller is still a bit applicable.
 *
 * MailController sends mails, it has the following configuration in application.conf:
 *      - mails.server: the mail server that has to be used. e.g. smtp.gmail.com
 *      - mails.serverPort: the port of the mailserver. e.g. 587
 *      - mails.account: the mail account from which the mails should be sent. e.g. mystaffexample@gmail.com
 *      - mails.password: the mail account's password. e.g. vakantie@5
 */


public class MailController {

    private static String from;
    private static String password;
    private static String host;
    private static int serverPort;
    private final boolean doLog;

    @Inject
    private AxiansController axiansController;


    /**
     * The constructor loads in the configuration from application.conf.
     */
    @Inject
    public MailController(Config config) {
        from = config.getString("mails.account");
        password = config.getString("mails.password");
        host = config.getString("mails.server");
        serverPort = config.getInt("mails.serverPort");
        doLog = config.getBoolean("mails.doLogSentMails");
    }

    /**
     * sendMail is the base function to send a mail to a list of email accounts.
     * source:  https://stackoverflow.com/questions/46663/how-can-i-send-an-email-by-java-application-using-gmail-yahoo-or-hotmail
     */
    public void sendMail(String[] to, String subject, String body) {
        if (doLog) {
            for (String receiver : to) {
                System.out.println("Sent mail with subject \"" + subject + "\" to \"" + receiver + "\"");
            }
        }
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", password);
        props.put("mail.smtp.port", serverPort + "");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAdress = new InternetAddress[to.length];

            for (int i = 0; i < to.length; i++) {
                toAdress[i] = new InternetAddress(to[i]);
            }

            for (int i = 0; i < toAdress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAdress[i]);
            }

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();


        } catch (AddressException ae) {
            ae.printStackTrace();
        } catch (MessagingException me) {
            me.printStackTrace();
        }
    }

    /**
     * This function is used when a mail should be sent to one email account.
     */
    public void sendMail(String to, String subject, String body) {
        sendMail(new String[]{to}, subject, body);
    }


    /**
     * Sends a mail to every planner for a certain person when he asked for a new absence.
     *
     * @param token: the login-token provided by Axians
     * @param hm:    the HolidayMessage-object that contains the new absence-request
     *               <p>
     *               Example mail in Dutch
     *               <p>
     *               SUBJECT: Verlofaanvraag THURSDAY 2 MAY 2019 door Raymond Cheung.
     *               <p>
     *               Beste Jesper
     *               <p>
     *               Er werd een nieuwe verlofaanvraag gedaan door Raymond Cheung voor THURSDAY 2 MAY 2019.
     *               U kan nu beslissen of u de aanvraag al dan niet goedkeurt.
     *               <p>
     *               Dit is een automatische e-mail.
     */
    public void sendMailNewHolidayMessageToPlanner(String token, HolidayMessage hm) {
        try {
            TranslateService ts = TranslateService.getInstance();
            Locale loc = new Locale(axiansController.getUserObject(token).getPreferredLocale());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", loc);
            ts.loadBundle(loc);
            Member member = getMemberWithId(hm.getEmployeeID(), token);
            PlanningUnitHelper puh = new PlanningUnitHelper(axiansController);
            List<PlanningMember> planners = puh.getAllPlannersOfPlanningUnitsWithIds(token, puh.getAllPlanningUnitIDsInWhichEmployeeIDIsIn(token, member.getId()));
            for(PlanningMember plannerpl: planners) {
                if(!plannerpl.getMember().equals(hm.getRequestByID()) && !hm.getEmployeeID().equals(plannerpl.getMember())) {
                    Member planner = getMemberWithId(plannerpl.getMember(), token);
                    StringBuilder mail = new StringBuilder(ts.getWord("GREETING") + " " + planner.getFirstName() + "\n\n");
                    List<ExactDate> exactDates = hm.getExactDates();
                    Collections.sort(exactDates);
                    ExactDate start = exactDates.get(0);
                    ExactDate end = exactDates.get(exactDates.size() - 1);
                    mail.append(ts.getWord("NEW_ABSENCE_REQUEST_BY") + " " + member.getFirstName() + " " + member.getLastName() + " " + ts.getWord("FOR") + " ");
                    String startString = start.getDate().format(df);
                    mail.append(startString);
                    StringBuilder subject = new StringBuilder(ts.getWord("ABSENCE_REQUEST") + " " + startString);
                    if (!start.getDate().equals(end.getDate())) {
                        String endString = end.getDate().format(df);
                        mail.append(" " + ts.getWord("TO") + " " + endString);
                        subject.append(" " + ts.getWord("TO") + " " + endString);
                    }
                    subject.append(" " + ts.getWord("BY") + " " + member.getFirstName() + " " + member.getLastName());
                    mail.append(".\n");
                    mail.append(ts.getWord("DECIDE_ABSENCE_REQUEST_APPROVAL") + ".\n\n");
                    sendMail(planner.getEmail(), subject.toString(), mail.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a confirmation mail when a person requests a new absence.
     *
     * @param token: the login-token provided by Axians
     * @param hm:    the HolidayMessage-object that contains the new absence-request
     *               <p>
     *               Example mail in Dutch
     *               <p>
     *               SUBJECT: Verlofaanvraag THURSDAY 2 MAY 2019
     *               <p>
     *               Beste Jesper
     *               <p>
     *               Uw verlof van THURSDAY 2 MAY 2019  werd goed ontvangen.
     *               U ontvangt nog een mail wanneer alle planners hebben beslist over uw verlof.
     *               <p>
     *               Dit is een automatische e-mail, indien u vragen hebt, gelieve u dan tot een planner te richten.
     */
    public void sendMailNewHolidayMessageBySelf(String token, HolidayMessage hm) {
        try {
            TranslateService ts = TranslateService.getInstance();
            Locale loc = new Locale(axiansController.getUserObject(token).getPreferredLocale());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", loc);
            ts.loadBundle(loc);

            Member member = getMemberWithId(hm.getEmployeeID(), token);
            StringBuilder mail = new StringBuilder(ts.getWord("GREETING") + " " + member.getFirstName() + "\n\n");
            List<ExactDate> exactDates = hm.getExactDates();
            Collections.sort(exactDates);
            ExactDate start = exactDates.get(0);
            ExactDate end = exactDates.get(exactDates.size() - 1);
            mail.append(ts.getWord("YOUR_ABSENCE_FROM") + " ");
            String startString = start.getDate().format(df);
            mail.append(startString);
            StringBuilder subject = new StringBuilder(ts.getWord("ABSENCE_REQUEST") + " " + startString);
            if (!start.getDate().equals(end.getDate())) {
                String endString = end.getDate().format(df);
                mail.append(" " + ts.getWord("TO") + " " + endString);
                subject.append(" " + ts.getWord("TO") + " " + endString);
            }
            mail.append(" " + ts.getWord("WAS_RECEIVED_WELL") + ".\n");
            mail.append(ts.getWord("EMAIL_WHEN_ALL_PLANNERS_HAVE_DECIDED") + ".\n\n");
            mail.append(ts.getWord("AUTOMATIC_EMAIL") + ".");



            sendMail(member.getEmail(), subject.toString(), mail.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a mail to an employee when a planner has requested an absence for the employee.
     *
     * @param token: the login-token provided by Axians
     * @param hm:    the HolidayMessage-object that contains the new absence-request
     *               <p>
     *               Example mail in Dutch
     *               <p>
     *               SUBJECT: Verlofaanvraag WEDNESDAY 8 MAY 2019 tot FRIDAY 10 MAY 2019
     *               <p>
     *               Beste Jesper
     *               <p>
     *               Een planner, Raymond Cheung deed voor u een verlofaanvraag van WEDNESDAY 8 MAY 2019 tot FRIDAY 10 MAY 2019 .
     *               U ontvangt nog een mail wanneer alle planners hebben beslist over uw verlof.
     *               <p>
     *               Dit is een automatische e-mail, indien u vragen hebt, gelieve u dan tot een planner te richten.
     */
    public void sendMailNewHolidayMessageByPlanner(String token, HolidayMessage hm) {
        try {
            TranslateService ts = TranslateService.getInstance();
            Locale loc = new Locale(axiansController.getUserObject(token).getPreferredLocale());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", loc);
            ts.loadBundle(loc);

            Member member = getMemberWithId(hm.getEmployeeID(), token);
            Member planner = getMemberWithId(hm.getRequestByID(), token);
            StringBuilder mail = new StringBuilder(ts.getWord("GREETING") + " " + member.getFirstName() + "\n\n");
            List<ExactDate> exactDates = hm.getExactDates();
            Collections.sort(exactDates);
            ExactDate start = exactDates.get(0);
            ExactDate end = exactDates.get(exactDates.size() - 1);
            mail.append(ts.getWord("A_PLANNER") + ", " + planner.getFirstName() + " " + planner.getLastName() + " " + ts.getWord("REQUEST_AN_ABSENCE_FOR_YOU_FROM") + " ");
            String startString = start.getDate().format(df);
            mail.append(startString);
            StringBuilder subject = new StringBuilder(ts.getWord("ABSENCE_REQUEST") + " " + startString);
            if (!start.getDate().equals(end.getDate())) {
                String endString = " " + ts.getWord("TO") + " " + end.getDate().format(df);
                mail.append(endString);
                subject.append(endString);
            }
            mail.append(".\n" + ts.getWord("EMAIL_WHEN_ALL_PLANNERS_HAVE_DECIDED") + ".\n\n");
            mail.append(ts.getWord("AUTOMATIC_EMAIL") + ".");

            sendMail(member.getEmail(), subject.toString(), mail.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a mail to an employee when he has modified an absence request.
     *
     * @param token: the login-token provided by Axians
     * @param hm:    the HolidayMessage-object that contains the modified absence-request
     *               <p>
     *               Example mail in Dutch
     *               <p>
     *               SUBJECT: Verlofaanvraag gewijzigd
     *               <p>
     *               Beste Jesper
     *               <p>
     *               Uw verlof van THURSDAY 9 MAY 2019  werd correct gewijzigd naar WEDNESDAY 8 MAY 2019 .
     *               U ontvangt nog een mail wanneer alle planners hebben beslist over uw nieuwe verlofaanvraag.
     *               <p>
     *               Dit is een automatische e-mail, indien u vragen hebt, gelieve u dan tot een planner te richten.
     */
    public void sendMailModificationHolidayMessage(String token, HolidayMessage hm) {
        try {
            TranslateService ts = TranslateService.getInstance();
            Locale loc = new Locale(axiansController.getUserObject(token).getPreferredLocale());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", loc);
            ts.loadBundle(loc);

            Member member = getMemberWithId(hm.getEmployeeID(), token);
            StringBuilder mail = new StringBuilder(ts.getWord("GREETING") + " " + member.getFirstName() + "\n\n");
            List<ExactDate> exactDates = hm.getExactDates();
            Collections.sort(exactDates);
            ExactDate start = exactDates.get(0);
            ExactDate end = exactDates.get(exactDates.size() - 1);

            HolidayMessageHistory hmh = hm.getHistory().get((hm.getHistory().size() - 1));
            List<ExactDateHMH> exactDateHMHS = hmh.getExactDates();
            Collections.sort(exactDateHMHS);
            ExactDateHMH startHMH = exactDateHMHS.get(0);
            ExactDateHMH endHMH = exactDateHMHS.get(exactDateHMHS.size() - 1);

            mail.append(ts.getWord("YOUR_ABSENCE_FROM") + " ");
            String startString = start.getDate().format(df) + " ";
            String startHMHString = startHMH.getDate().format(df) + " ";

            String endString = ts.getWord("TO") + " " + end.getDate().format(df);
            String endHMHString = ts.getWord("TO") + " " + endHMH.getDate().format(df) + " ";
            mail.append(startHMHString);

            if (!startHMH.getDate().equals(endHMH.getDate())) {
                mail.append(endHMHString);
            }
            mail.append(" " + ts.getWord("CHANGED_CORRECTLY"));
            if (!(startString.equals(startHMHString) && endString.equals(endHMHString))) {
                mail.append(" " + ts.getWord("TO_N") + " " + startString);
                if (!start.getDate().equals(end.getDate())) {
                    mail.append(endString);
                }
            }
            mail.append(".\n");

            mail.append("\n" + ts.getWord("EMAIL_WHEN_ALL_PLANNERS_HAVE_DECIDED") + ".\n\n");
            mail.append(ts.getWord("AUTOMATIC_EMAIL") + ".");

            StringBuilder subject = new StringBuilder(ts.getWord("ABSENCE_REQUEST_CHANGED"));

            sendMail(member.getEmail(), subject.toString(), mail.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a mail to an employee when he has deleted an absence request.
     *
     * @param token: the login-token provided by Axians
     * @param hm:    the HolidayMessage-object that contains the deleted absence-request
     *               <p>
     *               Example mail in Dutch
     *               <p>
     *               SUBJECT: Verlofaanvraag THURSDAY 9 MAY 2019 verwijderd
     *               <p>
     *               Beste Jesper
     *               <p>
     *               Uw verlof van THURSDAY 9 MAY 2019 werd succesvol verwijderd.
     *               <p>
     *               Dit is een automatische e-mail, indien u vragen hebt, gelieve u dan tot een planner te richten.
     */
    public void sendMailDeleteMessage(String token, HolidayMessage hm) {
        try {
            TranslateService ts = TranslateService.getInstance();
            Locale loc = new Locale(axiansController.getUserObject(token).getPreferredLocale());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", loc);
            ts.loadBundle(loc);

            Member member = getMemberWithId(hm.getEmployeeID(), token);
            StringBuilder mail = new StringBuilder(ts.getWord("GREETING") + " " + member.getFirstName() + "\n\n");
            List<ExactDate> exactDates = hm.getExactDates();
            Collections.sort(exactDates);
            ExactDate start = exactDates.get(0);
            ExactDate end = exactDates.get(exactDates.size() - 1);
            mail.append(ts.getWord("YOUR_ABSENCE_FROM") + " ");
            String startString = start.getDate().format(df);
            mail.append(startString + " ");
            StringBuilder subject = new StringBuilder(ts.getWord("ABSENCE_REQUEST") + " " + startString);
            if (!start.getDate().equals(end.getDate())) {
                String endString = end.getDate().format(df);
                mail.append(ts.getWord("TO") + " " + endString);
                subject.append(" " + ts.getWord("TO") + " " + endString);
            }
            subject.append(" " + ts.getWord("REMOVED"));
            mail.append(" " + ts.getWord("REMOVED_SUCCESSFULLY") + ".\n\n");
            mail.append(ts.getWord("AUTOMATIC_EMAIL") + ".");
            sendMail(member.getEmail(), subject.toString(), mail.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A HolidayMessage can only be approved if a planner of every planning unit the person is part of, has approved
     * the HolidayMessage.
     * This function checks if the global state of the HM is changed to "Approved" or "Rejected" and if so, sends the
     * correct mail.
     */
    public void sendMailNewHolidayPlanningsMessages(String token, HolidayPlanningsMessage hpm) {
        HolidayMessage hm = HolidayMessage.find.byId(hpm.id);
        boolean mailCanBeSendApproved = true;
        boolean mailCanBeSendRejected = false;
        for (PlanningUnitState pus : hm.getPlanningUnitStates()) {
            if (!pus.getState().equals(SchedulingState.Approved)) {
                mailCanBeSendApproved = false;
                if (pus.getState().equals(SchedulingState.Rejected)) {
                    mailCanBeSendRejected = true;
                }
            }
        }
        if (mailCanBeSendApproved) {
            sendMailNewHolidayPlanningsMessagesApproved(hm, token);
        } else if (mailCanBeSendRejected) {
            sendMailNewHolidayPlanningsMessagesRejected(hpm, hm, token);
        }
    }

    /**
     * Sends a mail to an employee when his absence request is globally approved.
     *
     * @param token: the login-token provided by Axians
     * @param hm:    the HolidayMessage-object that contains the approved absence-request
     *               <p>
     *               Example mail in Dutch
     *               <p>
     *               SUBJECT: Verlofaanvraag WEDNESDAY 15 MAY 2019 goedgekeurd
     *               <p>
     *               Beste Jesper
     *               <p>
     *               Uw verlof van WEDNESDAY 15 MAY 2019  werd goedgekeurd door alle planners.
     *               <p>
     *               Dit is een automatische e-mail, indien u vragen hebt, gelieve u dan tot een planner te richten.
     */
    private void sendMailNewHolidayPlanningsMessagesApproved(HolidayMessage hm, String token) {
        try {
            TranslateService ts = TranslateService.getInstance();
            Locale loc = new Locale(axiansController.getUserObject(token).getPreferredLocale());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", loc);
            ts.loadBundle(loc);

            Member member = getMemberWithId(hm.getEmployeeID(), token);
            StringBuilder mail = new StringBuilder(ts.getWord("GREETING") + " " + member.getFirstName() + "\n\n");
            List<ExactDate> exactDates = hm.getExactDates();
            Collections.sort(exactDates);
            ExactDate start = exactDates.get(0);
            ExactDate end = exactDates.get(exactDates.size() - 1);
            mail.append(ts.getWord("YOUR_ABSENCE_FROM") + " ");
            String startString = start.getDate().format(df) + " ";
            mail.append(startString);
            StringBuilder subject = new StringBuilder(ts.getWord("ABSENCE_REQUEST") + " " + startString);
            if (!start.getDate().equals(end.getDate())) {
                String endString = end.getDate().format(df);
                mail.append(" " + ts.getWord("TO") + " " + endString);
                subject.append(" " + ts.getWord("TO") + " " + endString);
            }
            subject.append(" " + ts.getWord("APPROVED"));
            mail.append(" " + ts.getWord("APPROVED_BY_ALL_PLANNERS") + ".\n\n");
            mail.append(ts.getWord("AUTOMATIC_EMAIL") + ".");

            sendMail(member.getEmail(), subject.toString(), mail.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a mail to an employee when his absence request is globally rejected.
     *
     * @param token: the login-token provided by Axians
     * @param hm:    the HolidayMessage-object that contains the rejected absence-request
     *               <p>
     *               Example mail in Dutch
     *               <p>
     *               SUBJECT: Verlofaanvraag THURSDAY 2 MAY 2019 afgekeurd
     *               <p>
     *               Beste Jesper
     *               <p>
     *               Uw verlof van THURSDAY 2 MAY 2019  werd afgekeurd door de planner Jesper Van Caeter.
     *               De planner gaf volgende reden: "Het inplannen van deze aanvraag maakt het onmogelijk om nog een planning te maken".
     *               <p>
     *               Dit is een automatische e-mail, indien u vragen hebt, dan kan u de planner bereiken via het e-mailadres: Jesper.VanCaeter@UGent.be.
     */
    private void sendMailNewHolidayPlanningsMessagesRejected(HolidayPlanningsMessage hpm, HolidayMessage hm, String token) {
        try {
            TranslateService ts = TranslateService.getInstance();
            Locale loc = new Locale(axiansController.getUserObject(token).getPreferredLocale());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", loc);
            ts.loadBundle(loc);

            Member member = getMemberWithId(hm.getEmployeeID(), token);
            Member planner = getMemberWithId(hm.getRequestByID(), token);
            StringBuilder mail = new StringBuilder(ts.getWord("GREETING") + " " + member.getFirstName() + "\n\n");
            List<ExactDate> exactDates = hm.getExactDates();
            Collections.sort(exactDates);
            ExactDate start = exactDates.get(0);
            ExactDate end = exactDates.get(exactDates.size() - 1);
            mail.append(ts.getWord("YOUR_ABSENCE_FROM") + " ");
            String startString = start.getDate().format(df) + " ";
            mail.append(startString);
            StringBuilder subject = new StringBuilder(ts.getWord("ABSENCE_REQUEST") + " " + startString);
            if (!start.getDate().equals(end.getDate())) {
                String endString = end.getDate().format(df);
                mail.append(" " + ts.getWord("TO") + endString);
                subject.append(" " + ts.getWord("TO") + " " + endString);
            }
            subject.append(" " + ts.getWord("REJECTED"));
            mail.append(" " + ts.getWord("REJECTED_BY_PLANNER") + " " + planner.getFirstName() + " " + planner.getLastName() + ".\n");
            if (hpm.getComment() == null) {
                mail.append(ts.getWord("NO_REASON") + ".");
            } else {
                mail.append(" " + ts.getWord("WITH_REASON") + ": \"" + hpm.getComment() + "\".\n\n");
            }
            mail.append("" + ts.getWord("AUTOMATIC_EMAIL_WITH_PLANNER_EMAIL") + ": " + planner.getEmail() + ".");

            sendMail(member.getEmail(), subject.toString(), mail.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple translation function that takes the id of a member and returns the Member object.
     * <p>
     * NOTE: to get one member, all members have to be loaded from Axians.
     */
    private Member getMemberWithId(String id, String token) throws Exception {
        Member[] members = axiansController.getMembersArray(token);
        for (Member member : members) {
            if (member.getId().equals(id)) {
                return member;
            }
        }
        return null;
    }
}
