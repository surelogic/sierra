package com.surelogic.sierra.jdbc.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.surelogic.sierra.tool.message.BugLinkServiceClient;
import com.surelogic.sierra.tool.message.ListCategoryRequest;
import com.surelogic.sierra.tool.message.ListCategoryResponse;
import com.surelogic.sierra.tool.message.ServerLocation;

public class CategoryUtil {

	public static void main(final String[] args) {
		final ServerLocation buglink = new ServerLocation("buglink.org", false,
				13376, "/sl/", "admin", "fluid!sl!ftw", true, false);
		writeCategories(
				buglink,
				new File(
						System.getProperty("user.dir")
								+ File.separator
								+ "src/com/surelogic/sierra/jdbc/settings/buglink-categories.xml"));
	}

	public static void writeCategories(final ServerLocation buglink,
			final File file) {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			final Marshaller m = JAXBContext.newInstance(
					ListCategoryResponse.class).createMarshaller();
			final FileOutputStream o = new FileOutputStream(file);
			try {
				m.setProperty("jaxb.formatted.output", true);
				m.marshal(BugLinkServiceClient.create(buglink).listCategories(
						new ListCategoryRequest()), o);
			} finally {
				o.flush();
				o.close();
			}
			System.out.println("Data written out to " + file.getPath());
		} catch (final JAXBException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
